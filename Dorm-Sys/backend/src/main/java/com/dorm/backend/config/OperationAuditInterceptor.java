package com.dorm.backend.config;

import com.dorm.backend.entity.OperationLog;
import com.dorm.backend.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class OperationAuditInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(OperationAuditInterceptor.class);
    private final OperationLogService operationLogService;

    public OperationAuditInterceptor(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String role = String.valueOf(request.getAttribute("currentUserRole"));
        if (!"admin".equals(role) || !isMutation(request.getMethod()) || request.getRequestURI().startsWith("/api/operationLog/")) return;
        try {
            OperationLog item = new OperationLog();
            item.setOperatorId(toLong(request.getAttribute("currentUserId")));
            item.setOperatorName(String.valueOf(request.getAttribute("currentUsername")));
            item.setModule(moduleOf(request.getRequestURI()));
            item.setAction(actionOf(request.getMethod()));
            item.setPath(request.getRequestURI());
            item.setResult(response.getStatus() < 400 && ex == null ? "成功" : "失败");
            item.setSummary(item.getAction() + " " + item.getModule());
            item.setCreateTime(LocalDateTime.now());
            operationLogService.save(item);
        } catch (Exception error) {
            log.warn("写入管理员操作日志失败", error);
        }
    }

    private boolean isMutation(String method) {
        return "POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method);
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) return number.longValue();
        return Long.valueOf(String.valueOf(value));
    }

    private String actionOf(String method) {
        return Map.of("POST", "新增", "PUT", "修改", "DELETE", "删除").getOrDefault(method.toUpperCase(), "变更");
    }

    private String moduleOf(String path) {
        if (path.startsWith("/api/user/")) return "用户权限";
        if (path.startsWith("/api/building/")) return "楼栋资源";
        if (path.startsWith("/api/room/") || path.startsWith("/api/bed/")) return "房间资源";
        if (path.startsWith("/api/businessRecord/")) return "公告管理";
        if (path.startsWith("/api/repair")) return "报修监控";
        return "系统管理";
    }
}
