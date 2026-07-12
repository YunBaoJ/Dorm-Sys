package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.entity.ChatMessage;
import com.dorm.backend.entity.User;
import com.dorm.backend.service.ChatMessageService;
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

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatMessageService chatMessageService;
    @Autowired
    private UserService userService;

    /**
     * Send a message (private or group)
     */
    @PostMapping("/send")
    public Result<Boolean> send(@RequestBody ChatMessage msg) {
        Long senderId = currentUserId();
        if (senderId == null) return Result.error(401, "未登录");
        msg.setSenderId(senderId);
        return Result.success(chatMessageService.save(msg));
    }

    /**
     * Get private chat history between current user and another user
     */
    @GetMapping("/private/{targetUserId}")
    public Result<List<ChatMessage>> getPrivateMessages(@PathVariable Long targetUserId) {
        Long myId = currentUserId();
        if (myId == null) return Result.error(401, "未登录");

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
        Long myId = currentUserId();
        if (myId == null) return Result.error(401, "未登录");

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
            if (targetId != null) latestByUser.putIfAbsent(targetId, message);
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

    private Long currentUserId() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attr != null) {
            Object val = attr.getRequest().getAttribute("currentUserId");
            return val instanceof Number ? ((Number) val).longValue() : null;
        }
        return null;
    }
}
