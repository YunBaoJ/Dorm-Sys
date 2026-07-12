package com.dorm.backend.controller;
import com.dorm.backend.entity.StayHistory;
import com.dorm.backend.service.StayHistoryService;
import com.dorm.backend.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;

@RestController
@RequestMapping("/api/stayHistory")
public class StayHistoryController {
    @Autowired
    private StayHistoryService stayHistoryService;

    @GetMapping("/list")
    public Result<List<StayHistory>> list() {
        Long userId = currentUserId();
        QueryWrapper<StayHistory> query = new QueryWrapper<>();
        if ("student".equals(currentUserRole())) query.eq("student_id", userId);
        return Result.success(stayHistoryService.list(query));
    }

    @GetMapping("/current")
    public Result<StayHistory> current() {
        Long userId = currentUserId();
        if (userId == null) return Result.error(401, "未登录");
        QueryWrapper<StayHistory> query = new QueryWrapper<>();
        query.eq("student_id", userId)
             .isNull("check_out_date")
             .orderByDesc("check_in_date")
             .last("LIMIT 1");
        return Result.success(stayHistoryService.getOne(query));
    }

    private Long currentUserId() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            Object value = attributes.getRequest().getAttribute("currentUserId");
            return value instanceof Number number ? number.longValue() : null;
        }
        return null;
    }

    private String currentUserRole() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            Object value = attributes.getRequest().getAttribute("currentUserRole");
            return value == null ? null : value.toString();
        }
        return null;
    }
}
