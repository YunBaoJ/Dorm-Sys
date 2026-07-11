package com.dorm.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.JwtUtils;
import com.dorm.backend.entity.User;
import com.dorm.backend.mapper.UserMapper;
import com.dorm.backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public Map<String, Object> login(String username, String password, String role) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username)
                    .eq("password", password)
                    .eq("role", role);
        
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new RuntimeException("账号或密码错误");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("role", user.getRole());
        
        // Remove password from returned user info
        user.setPassword(null);
        result.put("user", user);

        return result;
    }
}
