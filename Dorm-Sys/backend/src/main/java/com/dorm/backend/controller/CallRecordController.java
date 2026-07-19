package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.entity.CallRecord;
import com.dorm.backend.service.CallRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.dorm.backend.common.AuthUtils;
import com.dorm.backend.service.DormManagerScopeService;

@RestController
@RequestMapping("/api/callRecord")
public class CallRecordController {

    @Autowired
    private CallRecordService callRecordService;

    @Autowired
    private DormManagerScopeService managerScopeService;

    @GetMapping("/list")
    public Result list(@RequestParam(required = false) Long studentId) {
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
        List<CallRecord> list = callRecordService.list(queryWrapper);
        return Result.success(list);
    }

    @PostMapping("/add")
    public Result add(@RequestBody CallRecord callRecord) {
        if (AuthUtils.isStudent()) {
            callRecord.setStudentId(AuthUtils.getCurrentUserId());
        } else if ("dormmanager".equals(AuthUtils.getCurrentUserRole())
                && !managerScopeService.canManageStudent(AuthUtils.getCurrentUserId(), callRecord.getStudentId())) {
            return Result.error(403, "无权新增该学生来电记录");
        }
        callRecord.setStatus("PENDING");
        boolean save = callRecordService.save(callRecord);
        if (save) {
            return Result.success();
        }
        return Result.error("添加失败");
    }

    @PostMapping("/update")
    public Result update(@RequestBody CallRecord callRecord) {
        if (callRecord.getId() == null) {
            return Result.error("ID不能为空");
        }
        if (AuthUtils.isStudent()) return Result.error(403, "学生不能修改来电处理状态");
        CallRecord existing = callRecordService.getById(callRecord.getId());
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole()) && existing != null
                && !managerScopeService.canManageStudent(AuthUtils.getCurrentUserId(), existing.getStudentId())) {
            return Result.error(403, "无权修改该学生来电记录");
        }
        boolean update = callRecordService.updateById(callRecord);
        if (update) {
            return Result.success();
        }
        return Result.error("更新失败");
    }
}
