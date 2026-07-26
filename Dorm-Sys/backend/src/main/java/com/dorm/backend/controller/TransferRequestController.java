package com.dorm.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.common.AuthUtils;
import com.dorm.backend.entity.TransferRequest;
import com.dorm.backend.entity.User;
import com.dorm.backend.entity.Bed;
import com.dorm.backend.entity.Room;
import com.dorm.backend.entity.Building;
import com.dorm.backend.service.TransferRequestService;
import com.dorm.backend.service.UserService;
import com.dorm.backend.service.BedService;
import com.dorm.backend.service.RoomService;
import com.dorm.backend.service.BuildingService;
import com.dorm.backend.service.DormManagerScopeService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transferRequest")
public class TransferRequestController {

    private final TransferRequestService transferRequestService;
    private final UserService userService;
    private final BedService bedService;
    private final RoomService roomService;
    private final BuildingService buildingService;
    private final DormManagerScopeService managerScopeService;

    public TransferRequestController(TransferRequestService transferRequestService, UserService userService,
                                     BedService bedService, RoomService roomService,
                                     BuildingService buildingService, DormManagerScopeService managerScopeService) {
        this.transferRequestService = transferRequestService;
        this.userService = userService;
        this.bedService = bedService;
        this.roomService = roomService;
        this.buildingService = buildingService;
        this.managerScopeService = managerScopeService;
    }

    @GetMapping("/list")
    public Result<List<TransferRequest>> list(@RequestParam(required = false) Long studentId,
                                              @RequestParam(required = false) String status,
                                              @RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "100") Integer size) {
        List<TransferRequest> list = transferRequestService.listTransferRequestsWithDetails(studentId, status,
                AuthUtils.getCurrentUserRole(), AuthUtils.getCurrentUserId());
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<TransferRequest> getById(@PathVariable Long id) {
        TransferRequest record = transferRequestService.getById(id);
        if (AuthUtils.isStudent() && record != null
                && !AuthUtils.getCurrentUserId().equals(record.getStudentId())) {
            return Result.error(403, "无权查看该调宿申请");
        } else if ("dormmanager".equals(AuthUtils.getCurrentUserRole()) && record != null
                && !canManageTransfer(record)) {
            return Result.error(403, "无权查看该调宿申请");
        }
        return Result.success(record);
    }

    @PostMapping("/save")
    @Transactional
    public Result<Boolean> save(@RequestBody TransferRequest transferRequest) {
        if (AuthUtils.isStudent()) {
            if (transferRequest.getId() != null) return Result.error(403, "学生不能审批或修改调宿申请");
            transferRequest.setStudentId(AuthUtils.getCurrentUserId());
            transferRequest.setStatus("PENDING");
        } else if ("dormmanager".equals(AuthUtils.getCurrentUserRole())) {
            TransferRequest existing = transferRequest.getId() == null
                ? null : transferRequestService.getById(transferRequest.getId());
            if (existing == null || !canManageTransfer(existing)) {
                return Result.error(403, "无权处理该调宿申请");
            }
            Long targetRoomId = transferRequest.getTargetRoomId() != null
                ? transferRequest.getTargetRoomId() : existing.getTargetRoomId();
            if (targetRoomId != null
                    && !managerScopeService.canManageRoom(AuthUtils.getCurrentUserId(), targetRoomId)) {
                return Result.error(403, "无权将学生调入该房间");
            }
            transferRequest.setStudentId(existing.getStudentId());
            transferRequest.setCurrentBedId(existing.getCurrentBedId());
            transferRequest.setReason(existing.getReason());
            transferRequest.setCreateTime(existing.getCreateTime());
            transferRequest.setTargetRoomId(targetRoomId);
        }
        if ("APPROVED".equals(transferRequest.getStatus())) {
            Result<Boolean> transferResult = applyApprovedTransfer(transferRequest);
            if (transferResult.getCode() != 200) {
                return transferResult;
            }
        }
        return Result.success(transferRequestService.saveOrUpdate(transferRequest));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        if (AuthUtils.isStudent()) return Result.error(403, "学生不能删除调宿申请");
        TransferRequest record = transferRequestService.getById(id);
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole()) && record != null && !canManageTransfer(record)) {
            return Result.error(403, "无权删除该调宿申请");
        }
        return Result.success(transferRequestService.removeById(id));
    }

    private boolean canManageTransfer(TransferRequest transferRequest) {
        Bed currentBed = transferRequest.getCurrentBedId() == null
            ? null : bedService.getById(transferRequest.getCurrentBedId());
        if (currentBed == null && transferRequest.getStudentId() != null) {
            currentBed = bedService.list(new QueryWrapper<Bed>()
                .eq("student_id", transferRequest.getStudentId()).last("LIMIT 1")).stream().findFirst().orElse(null);
        }
        return currentBed != null
            && managerScopeService.canManageRoom(AuthUtils.getCurrentUserId(), currentBed.getRoomId());
    }

    private Result<Boolean> applyApprovedTransfer(TransferRequest transferRequest) {
        if (transferRequest.getStudentId() == null) {
            return Result.error(400, "申请学生不能为空");
        }
        if (transferRequest.getTargetRoomId() == null) {
            return Result.error(400, "批准调宿时必须指定目标房间");
        }

        Bed currentBed = findCurrentBed(transferRequest);
        if (currentBed != null && transferRequest.getTargetRoomId().equals(currentBed.getRoomId())) {
            return Result.success(true);
        }

        Bed targetBed = findAvailableTargetBed(transferRequest.getTargetRoomId());
        if (targetBed == null) {
            return Result.error(400, "目标房间暂无可用床位");
        }

        if (currentBed != null && !currentBed.getId().equals(targetBed.getId())) {
            releaseBed(currentBed);
        }

        targetBed.setStudentId(transferRequest.getStudentId());
        targetBed.setStatus("OCCUPIED");
        bedService.updateById(targetBed);
        releaseOtherBedsForStudent(transferRequest.getStudentId(), targetBed.getId());
        transferRequest.setCurrentBedId(currentBed != null ? currentBed.getId() : transferRequest.getCurrentBedId());

        refreshRoomStatus(currentBed != null ? currentBed.getRoomId() : null);
        refreshRoomStatus(targetBed.getRoomId());
        return Result.success(true);
    }

    private Bed findCurrentBed(TransferRequest transferRequest) {
        if (transferRequest.getCurrentBedId() != null) {
            Bed bed = bedService.getById(transferRequest.getCurrentBedId());
            if (bed != null && transferRequest.getStudentId().equals(bed.getStudentId())) {
                return bed;
            }
        }

        QueryWrapper<Bed> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_id", transferRequest.getStudentId());
        List<Bed> beds = bedService.list(queryWrapper);
        return beds.isEmpty() ? null : beds.get(0);
    }

    private Bed findAvailableTargetBed(Long targetRoomId) {
        QueryWrapper<Bed> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("room_id", targetRoomId);
        return bedService.list(queryWrapper).stream()
            .filter(bed -> bed.getStudentId() == null)
            .filter(bed -> bed.getStatus() == null || "EMPTY".equals(bed.getStatus()))
            .findFirst()
            .orElse(null);
    }

    private void releaseBed(Bed bed) {
        UpdateWrapper<Bed> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", bed.getId())
            .set("student_id", null)
            .set("status", "EMPTY");
        bedService.update(updateWrapper);
        bed.setStudentId(null);
        bed.setStatus("EMPTY");
    }

    private void releaseOtherBedsForStudent(Long studentId, Long keptBedId) {
        UpdateWrapper<Bed> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("student_id", studentId)
            .ne("id", keptBedId)
            .set("student_id", null)
            .set("status", "EMPTY");
        bedService.update(updateWrapper);
    }

    private void refreshRoomStatus(Long roomId) {
        if (roomId == null) {
            return;
        }

        Room room = roomService.getById(roomId);
        if (room == null || room.getCapacity() == null) {
            return;
        }

        QueryWrapper<Bed> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("room_id", roomId);
        long occupied = bedService.list(queryWrapper).stream()
            .filter(bed -> bed.getStudentId() != null || "OCCUPIED".equals(bed.getStatus()))
            .count();
        room.setStatus(occupied >= room.getCapacity() ? "FULL" : "NORMAL");
        roomService.updateById(room);
    }
}
