package com.dorm.backend.config;

import com.dorm.backend.common.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthInterceptorTest {

    @Test
    void studentCannotMutateDormitoryAdministrationResources() throws Exception {
        JwtUtils jwtUtils = new JwtUtils("test-secret-for-jwt-interceptor-test");
        JwtAuthInterceptor interceptor = new JwtAuthInterceptor(jwtUtils);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/room/save");
        request.addHeader("Authorization", "Bearer " + jwtUtils.generateToken(7L, "2022010001", "student"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(403);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/user/list", "/api/user/unassigned", "/api/bed/list", "/api/bed/1", "/api/dashboard/buildings"})
    void studentCannotReadManagementData(String path) throws Exception {
        JwtUtils jwtUtils = new JwtUtils("test-secret-for-jwt-interceptor-test");
        JwtAuthInterceptor interceptor = new JwtAuthInterceptor(jwtUtils);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        request.addHeader("Authorization", "Bearer " + jwtUtils.generateToken(7L, "2022010001", "student"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void studentCanReadOwnDormitoryDashboard() throws Exception {
        JwtUtils jwtUtils = new JwtUtils("test-secret-for-jwt-interceptor-test");
        JwtAuthInterceptor interceptor = new JwtAuthInterceptor(jwtUtils);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/dashboard/dorm");
        request.addHeader("Authorization", "Bearer " + jwtUtils.generateToken(7L, "2022010001", "student"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertThat(allowed).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/dashboard/stats", "/api/dashboard/alerts"})
    void managerCannotReadAdministratorDashboard(String path) throws Exception {
        JwtUtils jwtUtils = new JwtUtils("test-secret-for-jwt-interceptor-test");
        JwtAuthInterceptor interceptor = new JwtAuthInterceptor(jwtUtils);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        request.addHeader("Authorization", "Bearer " + jwtUtils.generateToken(8L, "manager1", "dormmanager"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(403);
    }
}
