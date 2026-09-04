package com.dorm.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.common.AuthUtils;
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
        Bed existingBed = bed.getId() == null ? null : bedService.getById(bed.getId());
        if (bed.getId() != null && existingBed == null) {
            return Result.error(404, "床位不存在");
        }
        Long targetRoomId = bed.getRoomId();
        if (targetRoomId == null && bed.getId() != null) {
            targetRoomId = existingBed.getRoomId();
        }
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole()) && existingBed != null
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
                && !managerScopeService.canManageRoom(AuthUtils.getCurrentUserId(), targetRoomId)) {
            return Result.error(403, "无权修改该床位");
        }
        if (bed.getStudentId() != null) {
            String currentBedStatus = existingBed == null ? bed.getStatus() : existingBed.getStatus();
            if ("BROKEN".equals(currentBedStatus)) {
                return Result.error(400, "损坏的床位不能办理入住");
            }
            Room targetRoom = roomService.getById(targetRoomId);
            if (targetRoom != null && "MAINTENANCE".equals(targetRoom.getStatus())) {
                return Result.error(400, "维修中的房间不能办理入住");
            }
        }
        if (bed.getId() != null) {
            Set<Long> affectedRoomIds = new LinkedHashSet<>();
            affectedRoomIds.add(existingBed.getRoomId());
            affectedRoomIds.add(targetRoomId);
            if (existingBed != null) {
                Long oldStudentId = existingBed.getStudentId();
                Long newStudentId = bed.getStudentId();
                
                if (!Objects.equals(oldStudentId, newStudentId)) {
                    // Someone is moving out
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
                    // Someone is moving in
                    if (newStudentId != null) {
                        List<Bed> previousBeds = bedService.list(new QueryWrapper<Bed>()
                            .eq("student_id", newStudentId)
                            .ne("id", bed.getId()));
                        previousBeds.stream().map(Bed::getRoomId).forEach(affectedRoomIds::add);
                        // Clear the student's previous bed if any
                        if (!previousBeds.isEmpty() && !bedService.update(
                                new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<Bed>()
                                    .set("student_id", null)
                                    .set("status", "EMPTY")
                                    .eq("student_id", newStudentId)
                                    .ne("id", bed.getId()))) {
                            throw new IllegalStateException("清理学生原床位失败");
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
            }
            
            com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<Bed> updateWrapper = new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
            updateWrapper.eq("id", bed.getId());
            if (bed.getStudentId() == null) {
                updateWrapper.set("student_id", null);
            } else {
                updateWrapper.set("student_id", bed.getStudentId());
            }
            if (bed.getStatus() != null) updateWrapper.set("status", bed.getStatus());
            if (bed.getRoomId() != null) updateWrapper.set("room_id", bed.getRoomId());
            if (bed.getBedNumber() != null) updateWrapper.set("bed_number", bed.getBedNumber());
            
            if (!bedService.update(updateWrapper)) {
                throw new IllegalStateException("更新床位失败");
            }
            affectedRoomIds.stream().filter(Objects::nonNull).forEach(this::refreshRoomStatus);
            return Result.success(true);
        }
        return Result.success(bedService.save(bed));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        Bed bed = bedService.getById(id);
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole()) && bed != null
                && !managerScopeService.canManageRoom(AuthUtils.getCurrentUserId(), bed.getRoomId())) {
            return Result.error(403, "无权删除该床位");
        }
        return Result.success(bedService.removeById(id));
    }

    private void refreshRoomStatus(Long roomId) {
        Room room = roomService.getById(roomId);
        if (room == null || room.getCapacity() == null || "MAINTENANCE".equals(room.getStatus())) {
            return;
        }

        long occupied = bedService.list(new QueryWrapper<Bed>().eq("room_id", roomId)).stream()
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
