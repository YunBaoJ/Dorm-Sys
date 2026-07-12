package com.dorm.backend.controller;

import com.dorm.backend.common.Result;
import com.dorm.backend.entity.User;
import com.dorm.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Result<List<User>> list() {
        List<User> users = userService.list();
        users.forEach(this::hidePassword);
        return Result.success(users);
    }

    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody User user) {
        if ("student".equals(currentUserRole())) {
            Long userId = currentUserId();
            User existing = userService.getById(userId);
            if (existing == null) {
                return Result.error(404, "用户不存在");
            }
            existing.setName(user.getName());
            existing.setAvatar(user.getAvatar());
            existing.setClassName(user.getClassName());
            existing.setEmail(user.getEmail());
            existing.setPhone(user.getPhone());
            user = existing;
        }
        // Simple mock encryption for prototype (in production use BCrypt)
        if (user.getId() == null && user.getPassword() == null) {
            user.setPassword("123456"); // Default password
        } else if (user.getId() != null && (user.getPassword() == null || user.getPassword().isBlank())) {
            user.setPassword(null);
        }
        return Result.success(userService.saveOrUpdate(user));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(userService.removeById(id));
    }

    private void hidePassword(User user) {
        user.setPassword(null);
    }

    private Long currentUserId() {
        Object value = currentRequestAttribute("currentUserId");
        return value instanceof Number number ? number.longValue() : null;
    }

    private String currentUserRole() {
        Object value = currentRequestAttribute("currentUserRole");
        return value == null ? null : value.toString();
    }

    private Object currentRequestAttribute(String name) {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest().getAttribute(name);
        }
        return null;
    }
}
