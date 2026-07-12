package com.dorm.backend.common;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class JwtUtilsTest {

    @Test
    @SuppressWarnings("unchecked")
    void generatedTokenCanBeParsedForRequestAuthentication() throws Exception {
        JwtUtils jwtUtils = new JwtUtils();
        String token = jwtUtils.generateToken(12L, "2022010001", "student");

        Method parseToken;
        try {
            parseToken = JwtUtils.class.getMethod("parseToken", String.class);
        } catch (NoSuchMethodException exception) {
            fail("JwtUtils must expose parseToken for request authentication");
            return;
        }
        Map<String, Object> claims = (Map<String, Object>) parseToken.invoke(jwtUtils, token);

        assertThat(claims.get("userId")).isEqualTo(12L);
        assertThat(claims.get("username")).isEqualTo("2022010001");
        assertThat(claims.get("role")).isEqualTo("student");
    }
}
