package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.entity.RepairRequest;
import com.dorm.backend.entity.User;
import com.dorm.backend.entity.Room;
import com.dorm.backend.entity.Building;
import com.dorm.backend.entity.Bed;
import com.dorm.backend.service.RepairRequestService;
import com.dorm.backend.service.UserService;
import com.dorm.backend.service.RoomService;
import com.dorm.backend.service.BuildingService;
import com.dorm.backend.service.BedService;
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
@RequestMapping("/api/repairRequest")
public class RepairRequestController {

    @Autowired
    private RepairRequestService repairRequestService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private RoomService roomService;
    
    @Autowired
    private BuildingService buildingService;

    @Autowired
    private BedService bedService;

    @Autowired
    private DormManagerScopeService managerScopeService;

    @GetMapping("/list")
    public Result<List<RepairRequest>> list(@RequestParam(required = false) Long submitterId, 
                                            @RequestParam(required = false) String status) {
        QueryWrapper<RepairRequest> queryWrapper = new QueryWrapper<>();
        if (AuthUtils.isStudent()) submitterId = AuthUtils.getCurrentUserId();
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole())) {
            List<Long> roomIds = managerScopeService.managedRoomIds(AuthUtils.getCurrentUserId());
            if (roomIds.isEmpty()) return Result.success(List.of());
            queryWrapper.in("room_id", roomIds);
        }
        if (submitterId != null) queryWrapper.eq("submitter_id", submitterId);
        if (status != null && !status.isEmpty()) queryWrapper.eq("status", status);
        
        queryWrapper.orderByDesc("create_time");
        List<RepairRequest> list = repairRequestService.list(queryWrapper);
        
        Map<Long, String> userMap = userService.list().stream()
            .collect(Collectors.toMap(User::getId, User::getName));
            
        Map<Long, Room> roomMap = roomService.list().stream()
            .collect(Collectors.toMap(Room::getId, r -> r));
            
        Map<Long, String> buildingMap = buildingService.list().stream()
            .collect(Collectors.toMap(Building::getId, Building::getName));
            
        for (RepairRequest req : list) {
            req.setSubmitterName(userMap.get(req.getSubmitterId()));
            if (req.getHandlerId() != null) req.setHandlerName(userMap.get(req.getHandlerId()));
            
            Room r = roomMap.get(req.getRoomId());
            if (r != null) {
                String bName = buildingMap.get(r.getBuildingId());
                req.setRoomName((bName != null ? bName : "") + " " + r.getRoomNumber());
            }
        }
        
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
