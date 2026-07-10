package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.JwtUtils;
import com.dorm.backend.common.Result;
import com.dorm.backend.dto.LoginDTO;
import com.dorm.backend.entity.User;
import com.dorm.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO loginDTO) {
        // Find user by username and role
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", loginDTO.getUsername())
                    .eq("password", loginDTO.getPassword());
                    
        // 如果前端传了 role 并且不是特殊情况可以加这个查询条件
        // .eq("role", loginDTO.getRole());

        User user = userService.getOne(queryWrapper);

        if (user == null) {
            return Result.error(401, "账号、密码错误或角色不匹配");
        }
        
        if (user.getEnabled() != null && !user.getEnabled()) {
            return Result.error(403, "账号已被停用");
        }

        // Generate JWT
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());

        // Return user info and token
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);

        return Result.success(data);
    }
}
