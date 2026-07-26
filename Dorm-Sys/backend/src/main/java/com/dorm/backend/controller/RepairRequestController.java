package com.dorm.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.common.AuthUtils;
import com.dorm.backend.entity.RepairRequest;
import com.dorm.backend.entity.Bed;
import com.dorm.backend.service.RepairRequestService;
import com.dorm.backend.service.BedService;
import com.dorm.backend.service.DormManagerScopeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/repairRequest")
public class RepairRequestController {

    private final RepairRequestService repairRequestService;
    private final BedService bedService;
    private final DormManagerScopeService managerScopeService;

    public RepairRequestController(RepairRequestService repairRequestService, BedService bedService,
                                   DormManagerScopeService managerScopeService) {
        this.repairRequestService = repairRequestService;
        this.bedService = bedService;
        this.managerScopeService = managerScopeService;
    }

    @GetMapping("/list")
    public Result<List<RepairRequest>> list(@RequestParam(required = false) Long submitterId, 
                                            @RequestParam(required = false) String status,
                                            @RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "100") Integer size) {
        List<RepairRequest> list = repairRequestService.listRepairRequestsWithDetails(submitterId, status,
                AuthUtils.getCurrentUserRole(), AuthUtils.getCurrentUserId());
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<RepairRequest> getById(@PathVariable Long id) {
        RepairRequest record = repairRequestService.getById(id);
        if (AuthUtils.isStudent() && record != null
                && !AuthUtils.getCurrentUserId().equals(record.getSubmitterId())) {
            return Result.error(403, "无权查看该报修记录");
        } else if ("dormmanager".equals(AuthUtils.getCurrentUserRole()) && record != null
                && !managerScopeService.canManageRoom(AuthUtils.getCurrentUserId(), record.getRoomId())) {
            return Result.error(403, "无权查看该报修记录");
        }
        return Result.success(record);
    }

    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody RepairRequest repairRequest) {
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole()) && repairRequest.getId() != null) {
            RepairRequest existing = repairRequestService.getById(repairRequest.getId());
            if (existing == null
                    || !managerScopeService.canManageRoom(AuthUtils.getCurrentUserId(), existing.getRoomId())) {
                return Result.error(403, "无权修改该报修记录");
            }
        }
        if (AuthUtils.isStudent()) {
            Long userId = AuthUtils.getCurrentUserId();
            if (repairRequest.getId() != null) {
                RepairRequest existing = repairRequestService.getById(repairRequest.getId());
                if (existing == null || !userId.equals(existing.getSubmitterId())) {
                    return Result.error(403, "无权修改该报修记录");
                }
                repairRequest.setStatus(existing.getStatus());
                repairRequest.setHandlerId(existing.getHandlerId());
            } else {
                repairRequest.setStatus("PENDING");
            }
            repairRequest.setSubmitterId(userId);
            Bed currentBed = bedService.list(new QueryWrapper<Bed>()
                .eq("student_id", userId).last("LIMIT 1")).stream().findFirst().orElse(null);
            if (currentBed == null || currentBed.getRoomId() == null) {
                return Result.error(400, "未找到当前宿舍，无法提交报修");
            }
            repairRequest.setRoomId(currentBed.getRoomId());
        }
        if (repairRequest.getRoomId() == null && repairRequest.getSubmitterId() != null) {
            QueryWrapper<Bed> bedQuery = new QueryWrapper<>();
            bedQuery.eq("student_id", repairRequest.getSubmitterId());
            Bed currentBed = bedService.list(bedQuery).stream().findFirst().orElse(null);
            if (currentBed != null) {
                repairRequest.setRoomId(currentBed.getRoomId());
            }
        }
        if (repairRequest.getRoomId() == null) {
            return Result.error(400, "未找到当前宿舍，无法提交报修");
        }
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole())
                && !managerScopeService.canManageRoom(AuthUtils.getCurrentUserId(), repairRequest.getRoomId())) {
            return Result.error(403, "无权修改该报修记录");
        }
        return Result.success(repairRequestService.saveOrUpdate(repairRequest));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        if (AuthUtils.isStudent()) return Result.error(403, "学生不能删除报修记录");
        RepairRequest record = repairRequestService.getById(id);
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole()) && record != null
                && !managerScopeService.canManageRoom(AuthUtils.getCurrentUserId(), record.getRoomId())) {
            return Result.error(403, "无权删除该报修记录");
        }
        return Result.success(repairRequestService.removeById(id));
    }
}
