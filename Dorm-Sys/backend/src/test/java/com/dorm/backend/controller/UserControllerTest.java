package com.dorm.backend.controller;

import com.dorm.backend.common.Result;
import com.dorm.backend.entity.User;
import com.dorm.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserControllerTest {

    @Test
    void listDoesNotReturnPasswords() {
        UserService userService = mock(UserService.class);
        User user = new User();
        user.setId(1L);
        user.setUsername("student");
        user.setPassword("123456");
        when(userService.list()).thenReturn(List.of(user));

        UserController controller = new UserController();
        ReflectionTestUtils.setField(controller, "userService", userService);

        Result<List<User>> result = controller.list();

        assertThat(result.getData()).extracting(User::getPassword).containsOnlyNulls();
    }
}
