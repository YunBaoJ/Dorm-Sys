package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.JwtUtils;
import com.dorm.backend.common.Result;
import com.dorm.backend.dto.LoginDTO;
import com.dorm.backend.entity.User;
import com.dorm.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void loginFiltersByRequestedRole() {
        UserService userService = mock(UserService.class);
        when(userService.getOne(any())).thenReturn(loginUser());

        AuthController controller = authController(userService);
        controller.login(loginDTO());

        ArgumentCaptor<QueryWrapper<User>> queryCaptor = ArgumentCaptor.forClass((Class) QueryWrapper.class);
        verify(userService).getOne(queryCaptor.capture());

        assertThat(queryCaptor.getValue().getSqlSegment()).contains("role");
    }

    @Test
    void loginDoesNotReturnPassword() {
        UserService userService = mock(UserService.class);
        when(userService.getOne(any())).thenReturn(loginUser());

        Result<Map<String, Object>> result = authController(userService).login(loginDTO());

        User returnedUser = (User) result.getData().get("user");
        assertThat(returnedUser.getPassword()).isNull();
    }

    private AuthController authController(UserService userService) {
        AuthController controller = new AuthController();
        ReflectionTestUtils.setField(controller, "userService", userService);
        ReflectionTestUtils.setField(controller, "jwtUtils", new JwtUtils());
        return controller;
    }

    private LoginDTO loginDTO() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("student");
        dto.setPassword("123456");
        dto.setRole("student");
        return dto;
    }

    private User loginUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("student");
        user.setPassword("123456");
        user.setRole("student");
        user.setEnabled(true);
        return user;
    }
}
