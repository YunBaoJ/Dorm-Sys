package com.dorm.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.common.AuthUtils;
import com.dorm.backend.common.BedAllocationConflictException;
import com.dorm.backend.entity.Bed;
import com.dorm.backend.entity.Room;
import com.dorm.backend.entity.User;
import com.dorm.backend.entity.StayHistory;
import com.dorm.backend.service.BedService;
import com.dorm.backend.service.RoomService;
import com.dorm.backend.service.UserService;
import com.dorm.backend.service.StayHistoryService;
import com.dorm.backend.service.DormManagerScopeService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Objects;

@RestController
@RequestMapping("/api/bed")
public class BedController {

    private final BedService bedService;
    private final UserService userService;
    private final StayHistoryService stayHistoryService;
    private final RoomService roomService;
    private final DormManagerScopeService managerScopeService;

    public BedController(BedService bedService, UserService userService,
                         StayHistoryService stayHistoryService, RoomService roomService,
                         DormManagerScopeService managerScopeService) {
        this.bedService = bedService;
        this.userService = userService;
        this.stayHistoryService = stayHistoryService;
        this.roomService = roomService;
        this.managerScopeService = managerScopeService;
    }

    @GetMapping("/list")
    public Result<List<Bed>> list(@RequestParam(required = false) Long roomId, @RequestParam(required = false) String status,
                                  @RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(defaultValue = "100") Integer size) {
        List<Bed> beds = bedService.listBedsWithDetails(roomId, status,
                AuthUtils.getCurrentUserRole(), AuthUtils.getCurrentUserId());
        return Result.success(beds);
    }

    @GetMapping("/{id}")
    public Result<Bed> getById(@PathVariable Long id) {
        Bed bed = bedService.getById(id);
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole()) && bed != null
                && !managerScopeService.canManageRoom(AuthUtils.getCurrentUserId(), bed.getRoomId())) {
            return Result.error(403, "无权查看该床位");
        }
        return Result.success(bed);
    }

    @PostMapping("/save")
    @Transactional
    public Result<Boolean> save(@RequestBody Bed bed) {
        if (bed.getId() == null) {
            if ("dormmanager".equals(AuthUtils.getCurrentUserRole())
                    && !managerScopeService.canManageRoom(AuthUtils.getCurrentUserId(), bed.getRoomId())) {
                return Result.error(403, "无权修改该床位");
            }
            return Result.error(400, "请通过房间管理创建床位");
        }
        Bed existingBed = bedService.getById(bed.getId());
        if (existingBed == null) {
            return Result.error(404, "床位不存在");
        }
        Long requestedRoomId = bed.getRoomId() != null ? bed.getRoomId() : existingBed.getRoomId();
        Long targetRoomId = existingBed.getRoomId();
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole())
                && !managerScopeService.canManageRoom(AuthUtils.getCurrentUserId(), existingBed.getRoomId())) {
            return Result.error(403, "无权修改该床位");
        }
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole()) && bed.getStudentId() != null) {
            Bed assignedBed = bedService.list(new QueryWrapper<Bed>()
                .eq("student_id", bed.getStudentId()).last("LIMIT 1")).stream().findFirst().orElse(null);
            if (assignedBed != null
                    && !managerScopeService.canManageRoom(AuthUtils.getCurrentUserId(), assignedBed.getRoomId())) {
                return Result.error(403, "无权调整其他楼栋学生的床位");
            }
        }
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole())
                && !managerScopeService.canManageRoom(AuthUtils.getCurrentUserId(), requestedRoomId)) {
            return Result.error(403, "无权修改该床位");
        }
        if (bed.getRoomId() != null && !Objects.equals(bed.getRoomId(), existingBed.getRoomId())) {
            return Result.error(400, "不能通过床位管理调整所属房间");
        }

        Long oldStudentId = existingBed.getStudentId();
        Long newStudentId = bed.getStudentId();
        List<Bed> previousBeds = List.of();
        if (!Objects.equals(oldStudentId, newStudentId) && newStudentId != null) {
            previousBeds = bedService.list(new QueryWrapper<Bed>()
                .eq("student_id", newStudentId)
                .ne("id", bed.getId()));
        }

        Set<Long> affectedRoomIds = new LinkedHashSet<>();
        affectedRoomIds.add(existingBed.getRoomId());
        previousBeds.stream().map(Bed::getRoomId).filter(Objects::nonNull).forEach(affectedRoomIds::add);
        Map<Long, Room> lockedRooms = lockRooms(affectedRoomIds);
        if (lockedRooms.size() != affectedRoomIds.stream().filter(Objects::nonNull).count()) {
            throw new BedAllocationConflictException("床位所属房间已变化，请刷新后重试");
        }

        if (bed.getStudentId() != null) {
            if ("BROKEN".equals(existingBed.getStatus())) {
                return Result.error(400, "损坏的床位不能办理入住");
            }
            Room targetRoom = lockedRooms.get(targetRoomId);
            if (targetRoom != null && "MAINTENANCE".equals(targetRoom.getStatus())) {
                return Result.error(400, "维修中的房间不能办理入住");
            }
        }

        if (!Objects.equals(oldStudentId, newStudentId)) {
            if (oldStudentId != null) {
                QueryWrapper<StayHistory> query = new QueryWrapper<>();
                query.eq("student_id", oldStudentId)
                    .eq("bed_id", bed.getId())
                    .isNull("check_out_date")
                    .orderByDesc("check_in_date")
                    .last("LIMIT 1");
                StayHistory history = stayHistoryService.getOne(query);
                if (history != null) {
                    history.setCheckOutDate(new Date());
                    if (!stayHistoryService.updateById(history)) {
                        throw new IllegalStateException("关闭住宿记录失败");
                    }
                }
            }

            if (newStudentId != null) {
                for (Bed previousBed : previousBeds) {
                    com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<Bed> releasePrevious =
                        new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
                    releasePrevious.eq("id", previousBed.getId())
                        .eq("room_id", previousBed.getRoomId())
                        .eq("student_id", newStudentId);
                    if (previousBed.getStatus() == null) {
                        releasePrevious.isNull("status");
                    } else {
                        releasePrevious.eq("status", previousBed.getStatus());
                    }
                    releasePrevious.set("student_id", null).set("status", "EMPTY");
                    if (!bedService.update(releasePrevious)) {
                        throw new BedAllocationConflictException("原床位状态已变化，请刷新后重试");
                    }
                }

                StayHistory oldHistory = stayHistoryService.getOne(new QueryWrapper<StayHistory>()
                    .eq("student_id", newStudentId)
                    .isNull("check_out_date")
                    .orderByDesc("check_in_date")
                    .last("LIMIT 1"));
                if (oldHistory != null) {
                    oldHistory.setCheckOutDate(new Date());
                    if (!stayHistoryService.updateById(oldHistory)) {
                        throw new IllegalStateException("关闭原住宿记录失败");
                    }
                }

                StayHistory newHistory = new StayHistory();
                newHistory.setStudentId(newStudentId);
                newHistory.setBedId(bed.getId());
                newHistory.setCheckInDate(new Date());
                if (!stayHistoryService.save(newHistory)) {
                    throw new IllegalStateException("创建住宿记录失败");
                }
            }
            bed.setStatus(newStudentId == null ? "EMPTY" : "OCCUPIED");
        }

        com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<Bed> updateWrapper =
            new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
        updateWrapper.eq("id", bed.getId())
            .eq("room_id", existingBed.getRoomId());
        if (existingBed.getStudentId() == null) {
            updateWrapper.isNull("student_id");
        } else {
            updateWrapper.eq("student_id", existingBed.getStudentId());
        }
        if (existingBed.getStatus() == null) {
            updateWrapper.isNull("status");
        } else {
            updateWrapper.eq("status", existingBed.getStatus());
        }
        if (bed.getStudentId() == null) {
            updateWrapper.set("student_id", null);
        } else {
            updateWrapper.set("student_id", bed.getStudentId());
        }
        if (bed.getStatus() != null) updateWrapper.set("status", bed.getStatus());
        if (bed.getBedNumber() != null) updateWrapper.set("bed_number", bed.getBedNumber());

        if (!bedService.update(updateWrapper)) {
            throw new BedAllocationConflictException("床位状态已变化，请刷新后重试");
        }
        affectedRoomIds.stream()
            .map(lockedRooms::get)
            .filter(Objects::nonNull)
            .forEach(this::refreshRoomStatus);
        return Result.success(true);
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

    @DeleteMapping("/{id}")
    @Transactional
    public Result<Boolean> delete(@PathVariable Long id) {
        Bed initialBed = bedService.getById(id);
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole()) && initialBed != null
                && !managerScopeService.canManageRoom(AuthUtils.getCurrentUserId(), initialBed.getRoomId())) {
            return Result.error(403, "无权删除该床位");
        }
        if (initialBed == null) {
            return Result.success(false);
        }

        Room room = roomService.getOne(new QueryWrapper<Room>()
            .eq("id", initialBed.getRoomId()).last("FOR UPDATE"));
        if (room == null) {
            return Result.error(409, "床位所属房间不存在");
        }
        List<Bed> roomBeds = bedService.list(new QueryWrapper<Bed>()
            .eq("room_id", room.getId()).last("FOR UPDATE"));
        Bed bed = roomBeds.stream().filter(item -> id.equals(item.getId())).findFirst().orElse(null);
        if (bed == null) {
            throw new BedAllocationConflictException("床位状态已变化，请刷新后重试");
        }
        if (bed.getStudentId() != null || "OCCUPIED".equals(bed.getStatus())) {
            return Result.error(400, "已入住的床位不能删除");
        }
        if (!stayHistoryService.list(new QueryWrapper<StayHistory>()
                .eq("bed_id", id).last("FOR UPDATE")).isEmpty()) {
            return Result.error(400, "存在住宿历史的床位不能删除");
        }
        if (roomBeds.size() <= 1) {
            return Result.error(400, "房间至少保留一个床位，请删除整个房间");
        }
        QueryWrapper<Bed> deleteQuery = new QueryWrapper<Bed>()
            .eq("id", id)
            .eq("room_id", room.getId())
            .isNull("student_id");
        if (bed.getStatus() == null) {
            deleteQuery.isNull("status");
        } else {
            deleteQuery.eq("status", bed.getStatus());
        }
        if (!bedService.remove(deleteQuery)) {
            throw new IllegalStateException("删除床位失败");
        }
        List<Bed> remainingBeds = roomBeds.stream().filter(item -> !id.equals(item.getId())).toList();
        synchronizeRoomAfterBedDeletion(room, remainingBeds);
        return Result.success(true);
    }

    private void synchronizeRoomAfterBedDeletion(Room room, List<Bed> remainingBeds) {
        int capacity = remainingBeds.size();
        room.setCapacity(capacity);
        if (!"MAINTENANCE".equals(room.getStatus())) {
            long occupied = remainingBeds.stream()
                .filter(item -> item.getStudentId() != null || "OCCUPIED".equals(item.getStatus()))
                .count();
            room.setStatus(capacity > 0 && occupied >= capacity ? "FULL" : "NORMAL");
        }
        if (!roomService.updateById(room)) {
            throw new IllegalStateException("同步房间容量失败");
        }
    }

    private void refreshRoomStatus(Room room) {
        if (room.getCapacity() == null || "MAINTENANCE".equals(room.getStatus())) {
            return;
        }

        long occupied = bedService.list(new QueryWrapper<Bed>()
                .eq("room_id", room.getId())
                .last("FOR UPDATE"))
            .stream()
            .filter(item -> item.getStudentId() != null || "OCCUPIED".equals(item.getStatus()))
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
