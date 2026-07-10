package com.dorm.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.entity.User;
import com.dorm.backend.mapper.UserMapper;
import com.dorm.backend.service.AuthService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    // JWT Secret Key (In production, this should be configured in properties)
    private static final SecretKey KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

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

        // Generate JWT token
        String token = Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24 hours
                .signWith(KEY)
                .compact();

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("role", user.getRole());
        
        // Remove password from returned user info
        user.setPassword(null);
        result.put("user", user);

        return result;
    }
}
