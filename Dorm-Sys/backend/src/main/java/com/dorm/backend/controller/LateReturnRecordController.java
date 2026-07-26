package com.dorm.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.common.AuthUtils;
import com.dorm.backend.entity.LateReturnRecord;
import com.dorm.backend.service.LateReturnRecordService;
import com.dorm.backend.service.DormManagerScopeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lateReturnRecord")
public class LateReturnRecordController {

    private final LateReturnRecordService lateReturnRecordService;
    private final DormManagerScopeService managerScopeService;

    public LateReturnRecordController(LateReturnRecordService lateReturnRecordService, DormManagerScopeService managerScopeService) {
        this.lateReturnRecordService = lateReturnRecordService;
        this.managerScopeService = managerScopeService;
    }

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "100") Integer size) {
        QueryWrapper<LateReturnRecord> queryWrapper = new QueryWrapper<>();
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole())) {
            List<Long> studentIds = managerScopeService.managedStudentIds(AuthUtils.getCurrentUserId());
            if (studentIds.isEmpty()) return Result.success(List.of());
            queryWrapper.in("student_id", studentIds);
        }
        queryWrapper.orderByDesc("create_time");
        Page<LateReturnRecord> pageResult = lateReturnRecordService.page(new Page<>(page, size), queryWrapper);
        return Result.success(pageResult.getRecords());
    }

    @PostMapping("/save")
    public Result save(@RequestBody LateReturnRecord entity) {
        if (entity.getId() == null) {
            // Add
            if ("dormmanager".equals(AuthUtils.getCurrentUserRole())
                    && !managerScopeService.canManageStudent(AuthUtils.getCurrentUserId(), entity.getStudentId())) {
                return Result.error(403, "无权新增该学生晚归记录");
            }
            return Result.success(lateReturnRecordService.save(entity));
        }
        // Update
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole())) {
            LateReturnRecord existing = lateReturnRecordService.getById(entity.getId());
            if (existing == null
                    || !managerScopeService.canManageStudent(AuthUtils.getCurrentUserId(), existing.getStudentId())) {
                return Result.error(403, "无权修改该晚归记录");
            }
            if (!managerScopeService.canManageStudent(AuthUtils.getCurrentUserId(), entity.getStudentId())) {
                return Result.error(403, "无权修改该学生晚归记录");
            }
        }
        return Result.success(lateReturnRecordService.updateById(entity));
    }
    
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        LateReturnRecord record = lateReturnRecordService.getById(id);
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole()) && record != null
                && !managerScopeService.canManageStudent(AuthUtils.getCurrentUserId(), record.getStudentId())) {
            return Result.error(403, "无权删除该学生晚归记录");
        }
        return Result.success(lateReturnRecordService.removeById(id));
    }
}
