package com.dorm.backend.config;

import com.dorm.backend.common.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthInterceptorTest {

    @Test
    void studentCannotMutateDormitoryAdministrationResources() throws Exception {
        JwtUtils jwtUtils = new JwtUtils();
        JwtAuthInterceptor interceptor = new JwtAuthInterceptor(jwtUtils);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/room/save");
        request.addHeader("Authorization", "Bearer " + jwtUtils.generateToken(7L, "2022010001", "student"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(403);
    }
}
