package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.entity.Bed;
import com.dorm.backend.entity.ChatMessage;
import com.dorm.backend.entity.User;
import com.dorm.backend.service.BedService;
import com.dorm.backend.service.ChatMessageService;
import com.dorm.backend.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatControllerTest {

    @AfterEach
    void clearRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void notificationsQueryCurrentStudentsInboundRoomMessages() {
        ChatMessageService chatService = mock(ChatMessageService.class);
        BedService bedService = mock(BedService.class);
        UserService userService = mock(UserService.class);

        Bed bed = new Bed();
        bed.setRoomId(10L);
        when(bedService.getOne(any(QueryWrapper.class))).thenReturn(bed);

        ChatMessage message = new ChatMessage();
        message.setId(5L);
        message.setSenderId(8L);
        message.setReceiverId(7L);
        message.setType("PRIVATE");
        message.setContent("hello");
        when(chatService.list(any(QueryWrapper.class))).thenReturn(List.of(message));

        User sender = new User();
        sender.setId(8L);
        sender.setName("Roommate");
        when(userService.listByIds(any())).thenReturn(List.of(sender));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("currentUserId", 7L);
        request.setAttribute("currentUserRole", "student");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        ChatController controller = new ChatController(chatService, userService, bedService);

        Result<List<ChatMessage>> result = controller.getNotifications();

        assertThat(result.getData()).singleElement().satisfies(item ->
            assertThat(item.getSenderName()).isEqualTo("Roommate")
        );
        ArgumentCaptor<QueryWrapper<ChatMessage>> queryCaptor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(chatService).list(queryCaptor.capture());
        String sql = queryCaptor.getValue().getCustomSqlSegment();
        assertThat(sql).contains("receiver_id", "room_id", "sender_id");
    }
}
