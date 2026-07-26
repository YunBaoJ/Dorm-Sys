package com.dorm.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.common.AuthUtils;
import com.dorm.backend.entity.Bed;
import com.dorm.backend.entity.User;
import com.dorm.backend.entity.StayHistory;
import com.dorm.backend.service.BedService;
import com.dorm.backend.service.UserService;
import com.dorm.backend.service.StayHistoryService;
import com.dorm.backend.service.DormManagerScopeService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.Objects;

@RestController
@RequestMapping("/api/bed")
public class BedController {

    private final BedService bedService;
    private final UserService userService;
    private final StayHistoryService stayHistoryService;
    private final DormManagerScopeService managerScopeService;

    public BedController(BedService bedService, UserService userService,
                         StayHistoryService stayHistoryService, DormManagerScopeService managerScopeService) {
        this.bedService = bedService;
        this.userService = userService;
        this.stayHistoryService = stayHistoryService;
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
        if (bed.getId() != null) {
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
                            stayHistoryService.updateById(history);
                        }
                    }
                    // Someone is moving in
                    if (newStudentId != null) {
                        // Clear the student's previous bed if any
                        bedService.update(new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<Bed>()
                            .set("student_id", null)
                            .set("status", "EMPTY")
                            .eq("student_id", newStudentId)
                            .ne("id", bed.getId()));
                            
                        StayHistory oldHistory = stayHistoryService.getOne(new QueryWrapper<StayHistory>()
                             .eq("student_id", newStudentId)
                             .isNull("check_out_date")
                             .orderByDesc("check_in_date")
                             .last("LIMIT 1"));
                        if (oldHistory != null) {
                            oldHistory.setCheckOutDate(new Date());
                            stayHistoryService.updateById(oldHistory);
                        }

                        StayHistory newHistory = new StayHistory();
                        newHistory.setStudentId(newStudentId);
                        newHistory.setBedId(bed.getId());
                        newHistory.setCheckInDate(new Date());
                        stayHistoryService.save(newHistory);
                    }
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
            
            bedService.update(updateWrapper);
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
}
