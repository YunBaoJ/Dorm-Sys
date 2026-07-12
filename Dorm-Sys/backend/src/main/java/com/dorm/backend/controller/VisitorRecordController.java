package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.entity.VisitorRecord;
import com.dorm.backend.entity.User;
import com.dorm.backend.service.VisitorRecordService;
import com.dorm.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/visitorRecord")
public class VisitorRecordController {

    @Autowired
    private VisitorRecordService visitorRecordService;
    
    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Result<List<VisitorRecord>> list(@RequestParam(required = false) Long studentId) {
        QueryWrapper<VisitorRecord> qw = new QueryWrapper<>();
        if (isStudent()) studentId = currentUserId();
        if (studentId != null) qw.eq("student_id", studentId);
        qw.orderByDesc("create_time");
        
        List<VisitorRecord> list = visitorRecordService.list(qw);
        Map<Long, String> userMap = userService.list().stream()
            .collect(Collectors.toMap(User::getId, User::getName));
        for (VisitorRecord vr : list) {
            vr.setStudentName(userMap.get(vr.getStudentId()));
        }
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<VisitorRecord> getById(@PathVariable Long id) {
        return Result.success(visitorRecordService.getById(id));
    }

    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody VisitorRecord visitorRecord) {
        if (isStudent()) {
            Long userId = currentUserId();
            if (visitorRecord.getId() != null) {
                VisitorRecord existing = visitorRecordService.getById(visitorRecord.getId());
                if (existing == null || !userId.equals(existing.getStudentId())) {
                    return Result.error(403, "无权修改该访客记录");
                }
                visitorRecord.setStatus(existing.getStatus());
            } else {
                visitorRecord.setStatus("PENDING");
            }
            visitorRecord.setStudentId(userId);
        }
        return Result.success(visitorRecordService.saveOrUpdate(visitorRecord));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        if (isStudent()) return Result.error(403, "学生不能删除访客记录");
        return Result.success(visitorRecordService.removeById(id));
    }

    private boolean isStudent() { return "student".equals(currentUserRole()); }
    private Long currentUserId() {
        Object value = currentRequestAttribute("currentUserId");
        return value instanceof Number number ? number.longValue() : null;
    }
    private String currentUserRole() {
        Object value = currentRequestAttribute("currentUserRole");
        return value == null ? null : value.toString();
    }
    private Object currentRequestAttribute(String name) {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest().getAttribute(name);
        }
        return null;
    }
}
