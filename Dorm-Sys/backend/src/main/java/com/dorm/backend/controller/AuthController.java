package com.dorm.backend.controller;

import com.dorm.backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String role = body.get("role");

        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> data = authService.login(username, password, role);
            response.put("code", 200);
            response.put("message", "登录成功");
            response.put("data", data);
        } catch (Exception e) {
            response.put("code", 401);
            response.put("message", e.getMessage());
        }
        return response;
    }
}
