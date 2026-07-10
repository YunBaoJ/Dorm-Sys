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
        return Result.success(userService.list());
    }

    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody User user) {
        // Simple mock encryption for prototype (in production use BCrypt)
        if (user.getId() == null && user.getPassword() == null) {
            user.setPassword("123456"); // Default password
        }
        return Result.success(userService.saveOrUpdate(user));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(userService.removeById(id));
    }
}
