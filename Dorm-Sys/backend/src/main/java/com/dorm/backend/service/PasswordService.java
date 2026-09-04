package com.dorm.backend.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) return false;
        if (isEncoded(storedPassword)) return encoder.matches(rawPassword, storedPassword);
        // 仅兼容旧的裸明文数据；空值和疑似其他编码格式不应退化为明文校验。
        if (storedPassword.isBlank() || storedPassword.startsWith("$") || storedPassword.startsWith("{")) return false;
        return rawPassword.equals(storedPassword);
    }

    public boolean isEncoded(String password) {
        return password != null && password.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
    }
}
