package com.dorm.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.common.AuthUtils;
import com.dorm.backend.common.BedAllocationConflictException;
import com.dorm.backend.entity.TransferRequest;
import com.dorm.backend.entity.User;
import com.dorm.backend.entity.Bed;
import com.dorm.backend.entity.Room;
import com.dorm.backend.entity.Building;
import com.dorm.backend.entity.StayHistory;
import com.dorm.backend.service.TransferRequestService;
import com.dorm.backend.service.UserService;
import com.dorm.backend.service.BedService;
import com.dorm.backend.service.RoomService;
import com.dorm.backend.service.BuildingService;
import com.dorm.backend.service.StayHistoryService;
import com.dorm.backend.service.DormManagerScopeService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transferRequest")
public class TransferRequestController {

    private final TransferRequestService transferRequestService;
    private final UserService userService;
    private final BedService bedService;
    private final RoomService roomService;
    private final BuildingService buildingService;
    private final StayHistoryService stayHistoryService;
    private final DormManagerScopeService managerScopeService;

    public TransferRequestController(TransferRequestService transferRequestService, UserService userService,
                                     BedService bedService, RoomService roomService,
                                     BuildingService buildingService, StayHistoryService stayHistoryService,
                                     DormManagerScopeService managerScopeService) {
        this.transferRequestService = transferRequestService;
        this.userService = userService;
        this.bedService = bedService;
        this.roomService = roomService;
        this.buildingService = buildingService;
        this.stayHistoryService = stayHistoryService;
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
        boolean isStudent = AuthUtils.isStudent();
        TransferRequest existing = !isStudent && transferRequest.getId() != null
            ? transferRequestService.getOne(new QueryWrapper<TransferRequest>()
                .eq("id", transferRequest.getId())
                .last("FOR UPDATE"))
            : null;
        boolean studentLocked = false;
        if (isStudent) {
            if (transferRequest.getId() != null) return Result.error(403, "学生不能审批或修改调宿申请");
            transferRequest.setStudentId(AuthUtils.getCurrentUserId());
            transferRequest.setStatus("PENDING");
        } else if ("dormmanager".equals(AuthUtils.getCurrentUserRole())) {
            if (existing == null) {
                return Result.error(403, "无权处理该调宿申请");
            }
            if ("APPROVED".equals(transferRequest.getStatus())) {
                if (existing.getStudentId() == null || !lockStudent(existing.getStudentId())) {
                    return Result.error(400, "申请学生不存在");
                }
                studentLocked = true;
            }
            if (!canManageTransfer(existing)) {
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
            if (!studentLocked && (transferRequest.getStudentId() == null
                    || !lockStudent(transferRequest.getStudentId()))) {
                return Result.error(400, "申请学生不存在");
            }
            Result<Boolean> transferResult = applyApprovedTransfer(transferRequest);
            if (transferResult.getCode() != 200) {
                return transferResult;
            }
        }
        if (!transferRequestService.saveOrUpdate(transferRequest)) {
            throw new IllegalStateException("保存调宿申请失败");
        }
        return Result.success(true);
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
        if (currentBed != null && !Objects.equals(transferRequest.getStudentId(), currentBed.getStudentId())) {
            currentBed = null;
        }
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
        if (hasMultipleBedAssignments(transferRequest.getStudentId(), false)) {
            return Result.error(409, "该学生存在多个床位分配，请先清理异常数据");
        }
        Bed currentBed = findCurrentBed(transferRequest, false);
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole()) && currentBed != null
                && !managerScopeService.canManageRoom(AuthUtils.getCurrentUserId(), currentBed.getRoomId())) {
            return Result.error(403, "无权处理该调宿申请");
        }
        Set<Long> affectedRoomIds = new LinkedHashSet<>();
        affectedRoomIds.add(transferRequest.getTargetRoomId());
        if (currentBed != null) {
            affectedRoomIds.add(currentBed.getRoomId());
        }
        Map<Long, Room> lockedRooms = lockRooms(affectedRoomIds);
        Room targetRoom = lockedRooms.get(transferRequest.getTargetRoomId());
        if (targetRoom == null) {
            throw new BedAllocationConflictException("目标房间已变化，请刷新后重试");
        }
        if (currentBed != null && !lockedRooms.containsKey(currentBed.getRoomId())) {
            throw new BedAllocationConflictException("原床位所属房间已变化，请刷新后重试");
        }
        if ("MAINTENANCE".equals(targetRoom.getStatus())) {
            return Result.error(400, "维修中的房间不能作为调宿目标");
        }

        if (hasMultipleBedAssignments(transferRequest.getStudentId(), true)) {
            return Result.error(409, "该学生存在多个床位分配，请先清理异常数据");
        }
        Bed lockedCurrentBed = findCurrentBed(transferRequest, true);
        if (currentBed != null && (lockedCurrentBed == null
                || !Objects.equals(currentBed.getId(), lockedCurrentBed.getId())
                || !Objects.equals(currentBed.getRoomId(), lockedCurrentBed.getRoomId()))) {
            throw new BedAllocationConflictException("原床位状态已变化，请刷新后重试");
        }
        if (lockedCurrentBed != null && !affectedRoomIds.contains(lockedCurrentBed.getRoomId())) {
            throw new BedAllocationConflictException("原床位所属房间已变化，请刷新后重试");
        }
        currentBed = lockedCurrentBed;
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole()) && currentBed != null
                && !managerScopeService.canManageRoom(AuthUtils.getCurrentUserId(), currentBed.getRoomId())) {
            return Result.error(403, "无权处理该调宿申请");
        }
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

        UpdateWrapper<Bed> claimTarget = new UpdateWrapper<>();
        claimTarget.eq("id", targetBed.getId())
            .eq("room_id", targetBed.getRoomId())
            .isNull("student_id")
            .and(status -> status.isNull("status").or().eq("status", "EMPTY"))
            .set("student_id", transferRequest.getStudentId())
            .set("status", "OCCUPIED");
        if (!bedService.update(claimTarget)) {
            throw new BedAllocationConflictException("目标床位已被占用，请刷新后重试");
        }
        targetBed.setStudentId(transferRequest.getStudentId());
        targetBed.setStatus("OCCUPIED");
        updateStayHistory(transferRequest.getStudentId(), targetBed.getId());
        transferRequest.setCurrentBedId(currentBed != null ? currentBed.getId() : transferRequest.getCurrentBedId());

        refreshRoomStatus(currentBed != null ? lockedRooms.get(currentBed.getRoomId()) : null);
        refreshRoomStatus(lockedRooms.get(targetBed.getRoomId()));
        return Result.success(true);
    }

    private boolean lockStudent(Long studentId) {
        return userService.getOne(new QueryWrapper<User>()
            .eq("id", studentId)
            .last("FOR UPDATE")) != null;
    }

    private Map<Long, Room> lockRooms(Set<Long> roomIds) {
        List<Long> sortedRoomIds = roomIds.stream()
            .filter(Objects::nonNull)
            .distinct()
            .sorted()
            .toList();
        if (sortedRoomIds.isEmpty()) {
            return Map.of();
        }
        return roomService.list(new QueryWrapper<Room>()
                .in("id", sortedRoomIds)
                .orderByAsc("id")
                .last("FOR UPDATE"))
            .stream()
            .collect(Collectors.toMap(Room::getId, room -> room));
    }

    private boolean hasMultipleBedAssignments(Long studentId, boolean forUpdate) {
        QueryWrapper<Bed> queryWrapper = new QueryWrapper<Bed>()
            .eq("student_id", studentId)
            .orderByAsc("id");
        if (forUpdate) queryWrapper.last("FOR UPDATE");
        return bedService.list(queryWrapper).size() > 1;
    }

    private Bed findCurrentBed(TransferRequest transferRequest, boolean forUpdate) {
        if (transferRequest.getCurrentBedId() != null) {
            Bed bed = forUpdate
                ? bedService.getOne(new QueryWrapper<Bed>()
                    .eq("id", transferRequest.getCurrentBedId())
                    .last("FOR UPDATE"))
                : bedService.getById(transferRequest.getCurrentBedId());
            if (bed != null && transferRequest.getStudentId().equals(bed.getStudentId())) {
                return bed;
            }
        }

        QueryWrapper<Bed> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_id", transferRequest.getStudentId());
        if (forUpdate) queryWrapper.last("FOR UPDATE");
        List<Bed> beds = bedService.list(queryWrapper);
        return beds.isEmpty() ? null : beds.get(0);
    }

    private Bed findAvailableTargetBed(Long targetRoomId) {
        QueryWrapper<Bed> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("room_id", targetRoomId).last("FOR UPDATE");
        return bedService.list(queryWrapper).stream()
            .filter(bed -> bed.getStudentId() == null)
            .filter(bed -> bed.getStatus() == null || "EMPTY".equals(bed.getStatus()))
            .findFirst()
            .orElse(null);
    }

    private void releaseBed(Bed bed) {
        UpdateWrapper<Bed> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", bed.getId())
            .eq("room_id", bed.getRoomId());
        if (bed.getStudentId() == null) {
            updateWrapper.isNull("student_id");
        } else {
            updateWrapper.eq("student_id", bed.getStudentId());
        }
        if (bed.getStatus() == null) {
            updateWrapper.isNull("status");
        } else {
            updateWrapper.eq("status", bed.getStatus());
        }
        updateWrapper.set("student_id", null)
            .set("status", "EMPTY");
        if (!bedService.update(updateWrapper)) {
            throw new BedAllocationConflictException("原床位状态已变化，请刷新后重试");
        }
        bed.setStudentId(null);
        bed.setStatus("EMPTY");
    }

    private void updateStayHistory(Long studentId, Long targetBedId) {
        StayHistory currentHistory = stayHistoryService.getOne(new QueryWrapper<StayHistory>()
            .eq("student_id", studentId)
            .isNull("check_out_date")
            .orderByDesc("check_in_date")
            .last("LIMIT 1"));
        Date now = new Date();
        if (currentHistory != null) {
            currentHistory.setCheckOutDate(now);
            if (!stayHistoryService.updateById(currentHistory)) {
                throw new IllegalStateException("关闭原住宿记录失败");
            }
        }

        StayHistory newHistory = new StayHistory();
        newHistory.setStudentId(studentId);
        newHistory.setBedId(targetBedId);
        newHistory.setCheckInDate(now);
        if (!stayHistoryService.save(newHistory)) {
            throw new IllegalStateException("创建住宿记录失败");
        }
    }

    private void refreshRoomStatus(Room room) {
        if (room == null) {
            return;
        }

        if (room.getCapacity() == null || "MAINTENANCE".equals(room.getStatus())) {
            return;
        }

        QueryWrapper<Bed> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("room_id", room.getId()).last("FOR UPDATE");
        long occupied = bedService.list(queryWrapper).stream()
            .filter(bed -> bed.getStudentId() != null || "OCCUPIED".equals(bed.getStatus()))
            .count();
        String status = occupied >= room.getCapacity() ? "FULL" : "NORMAL";
        if (!status.equals(room.getStatus())) {
            room.setStatus(status);
            if (!roomService.updateById(room)) {
                throw new IllegalStateException("刷新房间状态失败");
            }
        }
    }
}
