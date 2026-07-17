package com.dorm.backend.common;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class AuthUtils {
    public static Long getCurrentUserId() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attr != null) {
            Object idObj = attr.getRequest().getAttribute("currentUserId");
            if (idObj != null) {
                return idObj instanceof Number ? ((Number) idObj).longValue() : Long.parseLong(idObj.toString());
            }
        }
        return null;
    }

    public static String getCurrentUserRole() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attr != null) {
            Object roleObj = attr.getRequest().getAttribute("currentUserRole");
            return roleObj != null ? roleObj.toString() : null;
        }
        return null;
    }

    public static boolean isStudent() {
        return "student".equals(getCurrentUserRole());
    }
}
