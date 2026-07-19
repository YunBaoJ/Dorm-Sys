package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.entity.FeeBill;
import com.dorm.backend.entity.Room;
import com.dorm.backend.entity.Building;
import com.dorm.backend.service.FeeBillService;
import com.dorm.backend.service.RoomService;
import com.dorm.backend.service.BuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.dorm.backend.common.AuthUtils;
import com.dorm.backend.entity.Bed;
import com.dorm.backend.service.BedService;
import com.dorm.backend.service.DormManagerScopeService;

@RestController
@RequestMapping("/api/feeBill")
public class FeeBillController {

    @Autowired
    private FeeBillService feeBillService;
    
    @Autowired
    private RoomService roomService;
    
    @Autowired
    private BuildingService buildingService;
    
    @Autowired
    private BedService bedService;

    @Autowired
    private DormManagerScopeService managerScopeService;

    @GetMapping("/list")
    public Result<List<FeeBill>> list(@RequestParam(required = false) Long roomId,
                                      @RequestParam(required = false) String status) {
        QueryWrapper<FeeBill> qw = new QueryWrapper<>();
        if (roomId != null) qw.eq("room_id", roomId);
        if (status != null && !status.isEmpty()) qw.eq("status", status);
        
        // Filter by manager's buildings
        String role = AuthUtils.getCurrentUserRole();
        Long userId = AuthUtils.getCurrentUserId();

        if ("student".equals(role) && userId != null) {
            Bed currentBed = bedService.getOne(new QueryWrapper<Bed>()
                .eq("student_id", userId)
                .last("LIMIT 1"));
            if (currentBed == null || currentBed.getRoomId() == null) {
                return Result.success(List.of());
            }
            roomId = currentBed.getRoomId();
            qw = new QueryWrapper<>();
            qw.eq("room_id", roomId);
            if (status != null && !status.isEmpty()) qw.eq("status", status);
        } else if ("dormmanager".equals(role) && userId != null) {
            List<Long> roomIds = managerScopeService.managedRoomIds(userId);
            if (roomIds.isEmpty() || (roomId != null && !roomIds.contains(roomId))) return Result.success(List.of());
            if (roomId == null) qw.in("room_id", roomIds);
        }
        
        qw.orderByDesc("create_time");
        List<FeeBill> list = feeBillService.list(qw);
        
        Map<Long, Room> roomMap = roomService.list().stream()
            .collect(Collectors.toMap(Room::getId, r -> r));
        Map<Long, String> buildingMap = buildingService.list().stream()
            .collect(Collectors.toMap(Building::getId, Building::getName));
            
        for (FeeBill bill : list) {
            Room r = roomMap.get(bill.getRoomId());
            if (r != null) {
                String bName = buildingMap.getOrDefault(r.getBuildingId(), "");
                bill.setRoomName(bName + " " + r.getRoomNumber());
            }
        }
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<FeeBill> getById(@PathVariable Long id) {
        FeeBill bill = feeBillService.getById(id);
        if ("student".equals(AuthUtils.getCurrentUserRole()) && bill != null) {
            Bed currentBed = bedService.getOne(new QueryWrapper<Bed>()
                .eq("student_id", AuthUtils.getCurrentUserId())
                .last("LIMIT 1"));
            if (currentBed == null || !bill.getRoomId().equals(currentBed.getRoomId())) {
                return Result.error(403, "无权查看该宿舍账单");
            }
        } else if ("dormmanager".equals(AuthUtils.getCurrentUserRole()) && bill != null
                && !managerScopeService.canManageRoom(AuthUtils.getCurrentUserId(), bill.getRoomId())) {
            return Result.error(403, "无权查看该宿舍账单");
        }
        return Result.success(bill);
    }

    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody FeeBill feeBill) {
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole()) && feeBill.getId() != null) {
            FeeBill existing = feeBillService.getById(feeBill.getId());
            if (existing == null
                    || !managerScopeService.canManageRoom(AuthUtils.getCurrentUserId(), existing.getRoomId())) {
                return Result.error(403, "无权修改该宿舍账单");
            }
        }
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole())
                && !managerScopeService.canManageRoom(AuthUtils.getCurrentUserId(), feeBill.getRoomId())) {
            return Result.error(403, "无权修改该宿舍账单");
        }
        return Result.success(feeBillService.saveOrUpdate(feeBill));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        FeeBill bill = feeBillService.getById(id);
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole()) && bill != null
                && !managerScopeService.canManageRoom(AuthUtils.getCurrentUserId(), bill.getRoomId())) {
            return Result.error(403, "无权删除该宿舍账单");
        }
        return Result.success(feeBillService.removeById(id));
    }
}
