package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.JwtUtils;
import com.dorm.backend.common.Result;
import com.dorm.backend.dto.LoginDTO;
import com.dorm.backend.entity.User;
import com.dorm.backend.service.UserService;
import com.dorm.backend.service.PasswordService;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final PasswordService passwordService;

    public AuthController(UserService userService, JwtUtils jwtUtils, PasswordService passwordService) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.passwordService = passwordService;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO loginDTO) {
        // Find the account first; password verification is performed against its stored hash.
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", loginDTO.getUsername())
                    .eq("role", loginDTO.getRole());

        User user = userService.getOne(queryWrapper);

        if (user == null || !passwordService.matches(loginDTO.getPassword(), user.getPassword())) {
            return Result.error(401, "账号、密码错误或角色不匹配");
        }
        
        if (user.getEnabled() != null && !user.getEnabled()) {
            return Result.error(403, "账号已被停用");
        }

        if (!passwordService.isEncoded(user.getPassword())) {
            userService.update(new UpdateWrapper<User>()
                .eq("id", user.getId())
                .set("password", passwordService.encode(loginDTO.getPassword())));
        }

        // Generate JWT
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());

        // Return user info and token
        user.setPassword(null);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);

        return Result.success(data);
    }
}
