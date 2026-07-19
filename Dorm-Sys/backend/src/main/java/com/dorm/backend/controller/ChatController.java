package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.entity.ChatMessage;
import com.dorm.backend.entity.Bed;
import com.dorm.backend.entity.User;
import com.dorm.backend.service.ChatMessageService;
import com.dorm.backend.service.BedService;
import com.dorm.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import com.dorm.backend.common.AuthUtils;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatMessageService chatMessageService;
    @Autowired
    private UserService userService;
    @Autowired
    private BedService bedService;

    /**
     * Send a message (private or group)
     */
    @PostMapping("/send")
    public Result<Boolean> send(@RequestBody ChatMessage msg) {
        Long senderId = AuthUtils.getCurrentUserId();
        if (senderId == null) return Result.error(401, "未登录");
        if (msg.getContent() == null || msg.getContent().isBlank() || msg.getContent().length() > 1000) {
            return Result.error(400, "消息内容长度应为1到1000字");
        }
        Bed myBed = currentBed(senderId);
        if (myBed == null) return Result.error(403, "当前未分配宿舍，无法使用宿舍聊天");
        if ("PRIVATE".equals(msg.getType())) {
            Bed targetBed = currentBed(msg.getReceiverId());
            if (targetBed == null || !Objects.equals(myBed.getRoomId(), targetBed.getRoomId())) {
                return Result.error(403, "只能与当前室友私聊");
            }
            msg.setRoomId(null);
        } else if ("GROUP".equals(msg.getType())) {
            msg.setReceiverId(null);
            msg.setRoomId(myBed.getRoomId());
        } else {
            return Result.error(400, "消息类型不正确");
        }
        msg.setSenderId(senderId);
        return Result.success(chatMessageService.save(msg));
    }

    /**
     * Get private chat history between current user and another user
     */
    @GetMapping("/private/{targetUserId}")
    public Result<List<ChatMessage>> getPrivateMessages(@PathVariable Long targetUserId) {
        Long myId = AuthUtils.getCurrentUserId();
        if (myId == null) return Result.error(401, "未登录");
        Bed myBed = currentBed(myId);
        Bed targetBed = currentBed(targetUserId);
        if (myBed == null || targetBed == null || !Objects.equals(myBed.getRoomId(), targetBed.getRoomId())) {
            return Result.error(403, "只能查看当前室友的私聊");
        }

        QueryWrapper<ChatMessage> qw = new QueryWrapper<>();
        qw.eq("type", "PRIVATE")
          .and(w -> w
              .and(inner -> inner.eq("sender_id", myId).eq("receiver_id", targetUserId))
              .or(inner -> inner.eq("sender_id", targetUserId).eq("receiver_id", myId))
          )
          .orderByAsc("create_time")
          .last("LIMIT 200");

        List<ChatMessage> messages = chatMessageService.list(qw);
        populateSenderInfo(messages);
        return Result.success(messages);
    }

    @GetMapping("/conversations")
    public Result<List<Map<String, Object>>> getPrivateConversations() {
        Long myId = AuthUtils.getCurrentUserId();
        if (myId == null) return Result.error(401, "未登录");
        Bed myBed = currentBed(myId);
        if (myBed == null) return Result.success(new ArrayList<>());

        QueryWrapper<ChatMessage> qw = new QueryWrapper<>();
        qw.eq("type", "PRIVATE")
          .and(w -> w.eq("sender_id", myId).or().eq("receiver_id", myId))
          .orderByDesc("create_time")
          .last("LIMIT 500");

        List<ChatMessage> messages = chatMessageService.list(qw);
        LinkedHashMap<Long, ChatMessage> latestByUser = new LinkedHashMap<>();
        for (ChatMessage message : messages) {
            Long targetId = Objects.equals(message.getSenderId(), myId)
                    ? message.getReceiverId() : message.getSenderId();
            Bed targetBed = currentBed(targetId);
            if (targetId != null && targetBed != null && Objects.equals(myBed.getRoomId(), targetBed.getRoomId())) {
                latestByUser.putIfAbsent(targetId, message);
            }
        }

        if (latestByUser.isEmpty()) return Result.success(new ArrayList<>());
        Map<Long, User> users = userService.listByIds(latestByUser.keySet()).stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        List<Map<String, Object>> conversations = new ArrayList<>();
        latestByUser.forEach((targetId, message) -> {
            User user = users.get(targetId);
            if (user == null) return;
            Map<String, Object> conversation = new LinkedHashMap<>();
            conversation.put("id", user.getId());
            conversation.put("name", user.getName());
            conversation.put("avatar", user.getAvatar());
            conversation.put("lastMessage", message.getContent());
            conversation.put("lastTime", message.getCreateTime());
            conversations.add(conversation);
        });
        return Result.success(conversations);
    }

    /**
     * Get group chat history for a room
     */
    @GetMapping("/group/{roomId}")
    public Result<List<ChatMessage>> getGroupMessages(@PathVariable Long roomId) {
        Bed myBed = currentBed(AuthUtils.getCurrentUserId());
        if (myBed == null || !Objects.equals(myBed.getRoomId(), roomId)) {
            return Result.error(403, "无权查看该宿舍群聊");
        }
        QueryWrapper<ChatMessage> qw = new QueryWrapper<>();
        qw.eq("type", "GROUP")
          .eq("room_id", roomId)
          .orderByAsc("create_time")
          .last("LIMIT 200");

        List<ChatMessage> messages = chatMessageService.list(qw);
        populateSenderInfo(messages);
        return Result.success(messages);
    }

    private void populateSenderInfo(List<ChatMessage> messages) {
        if (messages.isEmpty()) return;
        List<Long> senderIds = messages.stream()
                .map(ChatMessage::getSenderId)
                .distinct()
                .collect(Collectors.toList());
        List<User> users = userService.listByIds(senderIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        for (ChatMessage m : messages) {
            User u = userMap.get(m.getSenderId());
            if (u != null) {
                m.setSenderName(u.getName());
                m.setSenderAvatar(u.getAvatar());
            }
        }
    }

    private Bed currentBed(Long userId) {
        if (userId == null) return null;
        QueryWrapper<Bed> query = new QueryWrapper<>();
        query.eq("student_id", userId).last("LIMIT 1");
        return bedService.getOne(query);
    }

}
