package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.entity.LateReturnRecord;
import com.dorm.backend.service.LateReturnRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.dorm.backend.common.AuthUtils;
import com.dorm.backend.service.DormManagerScopeService;

@RestController
@RequestMapping("/api/lateReturnRecord")
public class LateReturnRecordController {

    @Autowired
    private LateReturnRecordService lateReturnRecordService;

    @Autowired
    private DormManagerScopeService managerScopeService;

    @GetMapping("/list")
    public Result list() {
        QueryWrapper<LateReturnRecord> queryWrapper = new QueryWrapper<>();
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole())) {
            List<Long> studentIds = managerScopeService.managedStudentIds(AuthUtils.getCurrentUserId());
            if (studentIds.isEmpty()) return Result.success(List.of());
            queryWrapper.in("student_id", studentIds);
        }
        queryWrapper.orderByDesc("create_time");
        return Result.success(lateReturnRecordService.list(queryWrapper));
    }

    @PostMapping("/add")
    public Result add(@RequestBody LateReturnRecord entity) {
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole())
                && !managerScopeService.canManageStudent(AuthUtils.getCurrentUserId(), entity.getStudentId())) {
            return Result.error(403, "无权新增该学生晚归记录");
        }
        boolean save = lateReturnRecordService.save(entity);
        if (save) {
            return Result.success();
        }
        return Result.error("添加失败");
    }

    @PostMapping("/update")
    public Result update(@RequestBody LateReturnRecord entity) {
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole())) {
            LateReturnRecord existing = entity.getId() == null ? null : lateReturnRecordService.getById(entity.getId());
            if (existing == null
                    || !managerScopeService.canManageStudent(AuthUtils.getCurrentUserId(), existing.getStudentId())) {
                return Result.error(403, "无权修改该晚归记录");
            }
        }
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole())
                && !managerScopeService.canManageStudent(AuthUtils.getCurrentUserId(), entity.getStudentId())) {
            return Result.error(403, "无权修改该学生晚归记录");
        }
        boolean update = lateReturnRecordService.updateById(entity);
        if (update) {
            return Result.success();
        }
        return Result.error("更新失败");
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
