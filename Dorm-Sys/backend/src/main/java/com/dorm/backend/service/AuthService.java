package com.dorm.backend.service;

import java.util.Map;

public interface AuthService {
    Map<String, Object> login(String username, String password, String role);
}
