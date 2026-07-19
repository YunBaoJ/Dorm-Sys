package com.dorm.backend.controller;

import com.dorm.backend.common.Result;
import com.dorm.backend.entity.User;
import com.dorm.backend.service.UserService;
import com.dorm.backend.service.StudentInfoService;
import com.dorm.backend.service.ManagerInfoService;
import com.dorm.backend.service.AdminInfoService;
import com.dorm.backend.service.PasswordService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

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
        ReflectionTestUtils.setField(controller, "studentInfoService", mock(StudentInfoService.class));
        ReflectionTestUtils.setField(controller, "managerInfoService", mock(ManagerInfoService.class));
        ReflectionTestUtils.setField(controller, "adminInfoService", mock(AdminInfoService.class));
        ReflectionTestUtils.setField(controller, "passwordService", new PasswordService());

        Result<List<User>> result = controller.list();

        assertThat(result.getData()).extracting(User::getPassword).containsOnlyNulls();
    }

    @Test
    void studentProfileUpdateCannotChangeRoleOrIdentity() {
        UserService userService = mock(UserService.class);
        User existing = new User();
        existing.setId(7L);
        existing.setUsername("2022010001");
        existing.setRole("student");
        existing.setEnabled(true);
        existing.setPassword("123456");
        when(userService.getById(7L)).thenReturn(existing);
        when(userService.saveOrUpdate(any())).thenReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("currentUserId", 7L);
        request.setAttribute("currentUserRole", "student");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        User submitted = new User();
        submitted.setId(99L);
        submitted.setUsername("admin");
        submitted.setRole("admin");
        submitted.setName("张伟");
        submitted.setPhone("13800138000");

        UserController controller = new UserController();
        ReflectionTestUtils.setField(controller, "userService", userService);
        ReflectionTestUtils.setField(controller, "studentInfoService", mock(StudentInfoService.class));
        ReflectionTestUtils.setField(controller, "managerInfoService", mock(ManagerInfoService.class));
        ReflectionTestUtils.setField(controller, "adminInfoService", mock(AdminInfoService.class));
        ReflectionTestUtils.setField(controller, "passwordService", new PasswordService());
        controller.save(submitted);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userService).saveOrUpdate(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(7L);
        assertThat(captor.getValue().getUsername()).isEqualTo("2022010001");
        assertThat(captor.getValue().getRole()).isEqualTo("student");
        RequestContextHolder.resetRequestAttributes();
    }
}
