package com.dorm.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.common.AuthUtils;
import com.dorm.backend.entity.VisitorRecord;
import com.dorm.backend.entity.User;
import com.dorm.backend.service.VisitorRecordService;
import com.dorm.backend.service.UserService;
import com.dorm.backend.service.DormManagerScopeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/visitorRecord")
public class VisitorRecordController {

    private final VisitorRecordService visitorRecordService;
    private final UserService userService;
    private final DormManagerScopeService managerScopeService;

    public VisitorRecordController(VisitorRecordService visitorRecordService, UserService userService,
                                   DormManagerScopeService managerScopeService) {
        this.visitorRecordService = visitorRecordService;
        this.userService = userService;
        this.managerScopeService = managerScopeService;
    }

    @GetMapping("/list")
    public Result<List<VisitorRecord>> list(@RequestParam(required = false) Long studentId,
                                            @RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "100") Integer size) {
        List<VisitorRecord> list = visitorRecordService.listVisitorRecordsWithDetails(studentId,
                AuthUtils.getCurrentUserRole(), AuthUtils.getCurrentUserId());
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<VisitorRecord> getById(@PathVariable Long id) {
        VisitorRecord record = visitorRecordService.getById(id);
        if (AuthUtils.isStudent() && record != null
                && !AuthUtils.getCurrentUserId().equals(record.getStudentId())) {
            return Result.error(403, "无权查看该访客记录");
        } else if ("dormmanager".equals(AuthUtils.getCurrentUserRole()) && record != null
                && !canManageStudent(record.getStudentId())) {
            return Result.error(403, "无权查看该访客记录");
        }
        return Result.success(record);
    }

    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody VisitorRecord visitorRecord) {
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole()) && visitorRecord.getId() != null) {
            VisitorRecord existing = visitorRecordService.getById(visitorRecord.getId());
            if (existing == null || !canManageStudent(existing.getStudentId())) {
                return Result.error(403, "无权处理该访客记录");
            }
        }
        if (AuthUtils.isStudent()) {
            Long userId = AuthUtils.getCurrentUserId();
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
        } else if ("dormmanager".equals(AuthUtils.getCurrentUserRole())
                && !canManageStudent(visitorRecord.getStudentId())) {
            return Result.error(403, "无权处理该访客记录");
        }
        return Result.success(visitorRecordService.saveOrUpdate(visitorRecord));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        if (AuthUtils.isStudent()) return Result.error(403, "学生不能删除访客记录");
        VisitorRecord record = visitorRecordService.getById(id);
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole()) && record != null
                && !canManageStudent(record.getStudentId())) {
            return Result.error(403, "无权删除该访客记录");
        }
        return Result.success(visitorRecordService.removeById(id));
    }

    private boolean canManageStudent(Long studentId) {
        return managerScopeService.canManageStudent(AuthUtils.getCurrentUserId(), studentId);
    }
}
