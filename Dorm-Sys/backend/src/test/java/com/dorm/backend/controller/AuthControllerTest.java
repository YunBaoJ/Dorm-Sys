package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.JwtUtils;
import com.dorm.backend.common.Result;
import com.dorm.backend.dto.LoginDTO;
import com.dorm.backend.entity.User;
import com.dorm.backend.service.UserService;
import com.dorm.backend.service.PasswordService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.atLeastOnce;

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

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void loginAcceptsBcryptPasswordWithoutQueryingByPassword() {
        UserService userService = mock(UserService.class);
        User user = loginUser();
        user.setPassword(new PasswordService().encode("123456"));
        when(userService.getOne(any())).thenReturn(user);

        Result<Map<String, Object>> result = authController(userService).login(loginDTO());

        ArgumentCaptor<QueryWrapper<User>> queryCaptor = ArgumentCaptor.forClass((Class) QueryWrapper.class);
        verify(userService).getOne(queryCaptor.capture());
        assertThat(result.getCode()).isEqualTo(200);
        assertThat(queryCaptor.getValue().getSqlSegment()).doesNotContain("password");
    }

    @Test
    void bcryptLoginSucceeds() {
        UserService userService = mock(UserService.class);
        User user = loginUser();
        user.setPassword(new PasswordService().encode("123456"));
        when(userService.getOne(any())).thenReturn(user);

        Result<Map<String, Object>> result = authController(userService).login(loginDTO());

        assertThat(result.getCode()).isEqualTo(200);
    }

    private AuthController authController(UserService userService) {
        return new AuthController(userService, new JwtUtils("test-secret-for-auth-controller-test"), new PasswordService());
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
        user.setPassword(new PasswordService().encode("123456"));
        user.setRole("student");
        user.setEnabled(true);
        return user;
    }
}
