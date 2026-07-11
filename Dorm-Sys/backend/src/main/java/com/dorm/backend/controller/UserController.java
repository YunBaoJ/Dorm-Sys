package com.dorm.backend.controller;

import com.dorm.backend.common.Result;
import com.dorm.backend.entity.User;
import com.dorm.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
}
