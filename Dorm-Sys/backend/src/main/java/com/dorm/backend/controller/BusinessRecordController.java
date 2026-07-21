package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.AuthUtils;
import com.dorm.backend.common.Result;
import com.dorm.backend.entity.BusinessRecord;
import com.dorm.backend.service.BusinessRecordService;
import com.dorm.backend.service.DormManagerScopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/businessRecord")
public class BusinessRecordController {

    @Autowired
    private BusinessRecordService businessRecordService;

    @Autowired
    private DormManagerScopeService managerScopeService;

    @GetMapping("/list")
    public Result<List<BusinessRecord>> list(@RequestParam(required = false) String type,
                                             @RequestParam(required = false) String status) {
        QueryWrapper<BusinessRecord> queryWrapper = new QueryWrapper<>();
        if (AuthUtils.isStudent()) {
            if (type == null || type.isBlank() || "feedback".equals(type)) {
                type = "feedback";
                queryWrapper.eq("creator_id", AuthUtils.getCurrentUserId());
            } else if ("admin_notice".equals(type)) {
                status = "已发布";
            } else if ("manager_messages".equals(type)) {
                status = "已发布";
            } else {
                return Result.error(403, "无权查看该类业务记录");
            }
        } else if ("dormmanager".equals(AuthUtils.getCurrentUserRole()) && "feedback".equals(type)) {
            List<Long> studentIds = managerScopeService.managedStudentIds(AuthUtils.getCurrentUserId());
            if (studentIds.isEmpty()) return Result.success(List.of());
            queryWrapper.in("creator_id", studentIds);
        }
        if (type != null && !type.isBlank()) queryWrapper.eq("type", type);
        if (status != null && !status.isBlank()) queryWrapper.eq("status", status);
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
        if ("admin_notice".equals(record.getType()) && !"admin".equals(AuthUtils.getCurrentUserRole())) {
            return Result.error(403, "仅管理员可维护全校公告");
        }

        if (AuthUtils.isStudent()) {
            if (!"feedback".equals(record.getType())) {
                return Result.error(403, "学生只能提交意见反馈");
            }
            Long userId = AuthUtils.getCurrentUserId();
            if (record.getId() != null) {
                BusinessRecord existing = businessRecordService.getById(record.getId());
                if (existing == null || !"feedback".equals(existing.getType())
                        || !userId.equals(existing.getCreatorId())) {
                    return Result.error(403, "无权修改该记录");
                }
                record.setStatus(existing.getStatus());
                record.setCreateTime(existing.getCreateTime());
            } else {
                record.setStatus("PENDING");
            }
            record.setType("feedback");
            record.setCreatorId(userId);
        } else if ("dormmanager".equals(AuthUtils.getCurrentUserRole())) {
            Long managerId = AuthUtils.getCurrentUserId();
            if ("feedback".equals(record.getType())) {
                BusinessRecord existing = record.getId() == null ? null : businessRecordService.getById(record.getId());
                if (existing == null || !"feedback".equals(existing.getType())
                        || !managerScopeService.canManageStudent(managerId, existing.getCreatorId())) {
                    return Result.error(403, "无权处理该反馈");
                }
                record.setCreatorId(existing.getCreatorId());
                record.setCreateTime(existing.getCreateTime());
            } else if ("manager_messages".equals(record.getType()) || "manager_memos".equals(record.getType())) {
                if (record.getId() != null) {
                    BusinessRecord existing = businessRecordService.getById(record.getId());
                    if (existing == null || !record.getType().equals(existing.getType())
                            || !managerId.equals(existing.getCreatorId())) {
                        return Result.error(403, "无权修改该记录");
                    }
                    record.setCreateTime(existing.getCreateTime());
                }
                record.setCreatorId(managerId);
            } else {
                return Result.error(403, "无权修改该类业务记录");
            }
        } else if ("admin".equals(AuthUtils.getCurrentUserRole()) && "admin_notice".equals(record.getType())) {
            if (!"草稿".equals(record.getStatus()) && !"已发布".equals(record.getStatus())) {
                return Result.error(400, "管理员公告状态仅支持草稿或已发布");
            }
            if (record.getId() != null) {
                BusinessRecord existing = businessRecordService.getById(record.getId());
                if (existing == null || !"admin_notice".equals(existing.getType())) {
                    return Result.error(403, "无权修改该记录");
                }
                record.setCreateTime(existing.getCreateTime());
            }
            record.setCreatorId(AuthUtils.getCurrentUserId());
        }

        LocalDateTime now = LocalDateTime.now();
        if ("admin_notice".equals(record.getType()) && "已发布".equals(record.getStatus())) {
            record.setEventTime(now);
        }
        if (record.getId() == null && record.getCreateTime() == null) record.setCreateTime(now);
        record.setUpdateTime(now);
        return Result.success(businessRecordService.saveOrUpdate(record));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        if (AuthUtils.isStudent()) return Result.error(403, "学生不能删除业务记录");
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole())) {
            BusinessRecord existing = businessRecordService.getById(id);
            if (existing == null || !("manager_messages".equals(existing.getType())
                    || "manager_memos".equals(existing.getType()))
                    || !AuthUtils.getCurrentUserId().equals(existing.getCreatorId())) {
                return Result.error(403, "无权删除该记录");
            }
        }
        return Result.success(businessRecordService.removeById(id));
    }
}
