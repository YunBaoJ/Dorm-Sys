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
import com.dorm.backend.common.AuthUtils;
import com.dorm.backend.service.DormManagerScopeService;

@RestController
@RequestMapping("/api/visitorRecord")
public class VisitorRecordController {

    @Autowired
    private VisitorRecordService visitorRecordService;
    
    @Autowired
    private UserService userService;

    @Autowired
    private DormManagerScopeService managerScopeService;

    @GetMapping("/list")
    public Result<List<VisitorRecord>> list(@RequestParam(required = false) Long studentId) {
        QueryWrapper<VisitorRecord> qw = new QueryWrapper<>();
        if (AuthUtils.isStudent()) studentId = AuthUtils.getCurrentUserId();
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole())) {
            List<Long> studentIds = managerScopeService.managedStudentIds(AuthUtils.getCurrentUserId());
            if (studentIds.isEmpty() || (studentId != null && !studentIds.contains(studentId))) return Result.success(List.of());
            if (studentId == null) qw.in("student_id", studentIds);
        }
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
