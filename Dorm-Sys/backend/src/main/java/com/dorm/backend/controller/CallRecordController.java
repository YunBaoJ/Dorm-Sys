package com.dorm.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.common.AuthUtils;
import com.dorm.backend.entity.CallRecord;
import com.dorm.backend.service.CallRecordService;
import com.dorm.backend.service.DormManagerScopeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/callRecord")
public class CallRecordController {

    private final CallRecordService callRecordService;
    private final DormManagerScopeService managerScopeService;

    public CallRecordController(CallRecordService callRecordService, DormManagerScopeService managerScopeService) {
        this.callRecordService = callRecordService;
        this.managerScopeService = managerScopeService;
    }

    @GetMapping("/list")
    public Result list(@RequestParam(required = false) Long studentId,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "100") Integer size) {
        QueryWrapper<CallRecord> queryWrapper = new QueryWrapper<>();
        if (AuthUtils.isStudent()) {
            studentId = AuthUtils.getCurrentUserId();
        } else if ("dormmanager".equals(AuthUtils.getCurrentUserRole())) {
            List<Long> studentIds = managerScopeService.managedStudentIds(AuthUtils.getCurrentUserId());
            if (studentIds.isEmpty() || (studentId != null && !studentIds.contains(studentId))) return Result.success(List.of());
            if (studentId == null) queryWrapper.in("student_id", studentIds);
        }
        if (studentId != null) {
            queryWrapper.eq("student_id", studentId);
        }
        queryWrapper.orderByDesc("create_time");
        Page<CallRecord> pageResult = callRecordService.page(new Page<>(page, size), queryWrapper);
        return Result.success(pageResult.getRecords());
    }

    @PostMapping("/save")
    public Result save(@RequestBody CallRecord callRecord) {
        if (callRecord.getId() == null) {
            // Add
            if (AuthUtils.isStudent()) {
                callRecord.setStudentId(AuthUtils.getCurrentUserId());
            } else if ("dormmanager".equals(AuthUtils.getCurrentUserRole())
                    && !managerScopeService.canManageStudent(AuthUtils.getCurrentUserId(), callRecord.getStudentId())) {
                return Result.error(403, "无权新增该学生来电记录");
            }
            callRecord.setStatus("PENDING");
            return Result.success(callRecordService.save(callRecord));
        }
        // Update
        if (AuthUtils.isStudent()) return Result.error(403, "学生不能修改来电处理状态");
        CallRecord existing = callRecordService.getById(callRecord.getId());
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole()) && existing != null
                && !managerScopeService.canManageStudent(AuthUtils.getCurrentUserId(), existing.getStudentId())) {
            return Result.error(403, "无权修改该学生来电记录");
        }
        return Result.success(callRecordService.updateById(callRecord));
    }
}
