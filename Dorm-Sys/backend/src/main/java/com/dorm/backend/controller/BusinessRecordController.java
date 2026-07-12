package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.entity.BusinessRecord;
import com.dorm.backend.service.BusinessRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/businessRecord")
public class BusinessRecordController {

    @Autowired
    private BusinessRecordService businessRecordService;

    @GetMapping("/list")
    public Result<List<BusinessRecord>> list(@RequestParam(required = false) String type,
                                             @RequestParam(required = false) String status) {
        QueryWrapper<BusinessRecord> queryWrapper = new QueryWrapper<>();
        if (type != null && !type.isBlank()) {
            queryWrapper.eq("type", type);
        }
        if (status != null && !status.isBlank()) {
            queryWrapper.eq("status", status);
        }
        if (isStudent() && isStudentOwnedType(type)) {
            queryWrapper.eq("creator_id", currentUserId());
        }
        queryWrapper.orderByDesc("create_time");
        return Result.success(businessRecordService.list(queryWrapper));
    }

    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody BusinessRecord record) {
        if (record.getType() == null || record.getType().isBlank()) {
            return Result.error(400, "业务类型不能为空");
        }
        if (record.getTitle() == null || record.getTitle().isBlank()) {
            return Result.error(400, "标题不能为空");
        }

        if (isStudent() && isStudentOwnedType(record.getType())) {
            Long userId = currentUserId();
            if (record.getId() != null) {
                BusinessRecord existing = businessRecordService.getById(record.getId());
                if (existing == null || !userId.equals(existing.getCreatorId())) {
                    return Result.error(403, "无权修改该记录");
                }
                record.setStatus(existing.getStatus());
            } else if ("student_feedback".equals(record.getType())) {
                record.setStatus("待受理");
            } else if ("student_call".equals(record.getType())) {
                record.setStatus("待呼叫");
            }
            record.setCreatorId(userId);
        }

        LocalDateTime now = LocalDateTime.now();
        if (record.getId() == null && record.getCreateTime() == null) {
            record.setCreateTime(now);
        }
        record.setUpdateTime(now);
        return Result.success(businessRecordService.saveOrUpdate(record));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        if (isStudent()) {
            return Result.error(403, "学生不能删除业务记录");
        }
        return Result.success(businessRecordService.removeById(id));
    }

    private boolean isStudentOwnedType(String type) {
        return type != null && type.startsWith("student_");
    }

    private boolean isStudent() {
        return "student".equals(currentUserRole());
    }

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
