package com.dorm.backend.config;

import com.dorm.backend.common.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    public JwtAuthInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            writeUnauthorized(response, "请先登录");
            return false;
        }

        try {
            Map<String, Object> claims = jwtUtils.parseToken(authorization.substring(7));
            request.setAttribute("currentUserId", claims.get("userId"));
            request.setAttribute("currentUsername", claims.get("username"));
            request.setAttribute("currentUserRole", claims.get("role"));
            if (isAdminOnlyRead(request, String.valueOf(claims.get("role")))) {
                writeForbidden(response, "仅管理员可以查看该资源");
                return false;
            }
            if (isForbiddenStudentRead(request, String.valueOf(claims.get("role")))) {
                writeForbidden(response, "学生无权查看宿舍管理数据");
                return false;
            }
            if (isForbiddenStudentMutation(request, String.valueOf(claims.get("role")))) {
                writeForbidden(response, "学生无权修改宿舍管理资源");
                return false;
            }
            if (isAdminOnlyMutation(request, String.valueOf(claims.get("role")))) {
                writeForbidden(response, "仅管理员可以修改该资源");
                return false;
            }
            return true;
        } catch (Exception exception) {
            writeUnauthorized(response, "登录状态已失效，请重新登录");
            return false;
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\",\"data\":null}");
    }

    private boolean isForbiddenStudentMutation(HttpServletRequest request, String role) {
        if (!"student".equals(role) || "GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String path = request.getRequestURI();
        return path.startsWith("/api/room/")
                || path.startsWith("/api/building/")
                || path.startsWith("/api/bed/")
                || path.startsWith("/api/feeBill/")
                || (path.startsWith("/api/user/") && !"/api/user/save".equals(path));
    }

    private boolean isForbiddenStudentRead(HttpServletRequest request, String role) {
        if (!"student".equals(role) || !"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String path = request.getRequestURI();
        return path.startsWith("/api/user/")
                || path.startsWith("/api/bed/")
                || path.equals("/api/dashboard/buildings");
    }

    private boolean isAdminOnlyRead(HttpServletRequest request, String role) {
        if ("admin".equals(role) || !"GET".equalsIgnoreCase(request.getMethod())) return false;
        String path = request.getRequestURI();
        return path.equals("/api/dashboard/stats") || path.equals("/api/dashboard/alerts");
    }

    private void writeForbidden(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":403,\"message\":\"" + message + "\",\"data\":null}");
    }

    private boolean isAdminOnlyMutation(HttpServletRequest request, String role) {
        if ("admin".equals(role) || "GET".equalsIgnoreCase(request.getMethod())) return false;
        String path = request.getRequestURI();
        return path.startsWith("/api/building/")
                || path.startsWith("/api/room/");
    }
}
