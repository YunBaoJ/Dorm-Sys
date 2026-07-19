package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.entity.HygieneRecord;
import com.dorm.backend.entity.Room;
import com.dorm.backend.entity.Building;
import com.dorm.backend.entity.User;
import com.dorm.backend.service.HygieneRecordService;
import com.dorm.backend.service.RoomService;
import com.dorm.backend.service.BuildingService;
import com.dorm.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.dorm.backend.common.AuthUtils;
import com.dorm.backend.service.DormManagerScopeService;

@RestController
@RequestMapping("/api/hygieneRecord")
public class HygieneRecordController {

    @Autowired
    private HygieneRecordService hygieneRecordService;
    
    @Autowired
    private RoomService roomService;
    
    @Autowired
    private BuildingService buildingService;
    
    @Autowired
    private UserService userService;

    @Autowired
    private DormManagerScopeService managerScopeService;

    @GetMapping("/list")
    public Result<List<HygieneRecord>> list(@RequestParam(required = false) Long roomId) {
        QueryWrapper<HygieneRecord> qw = new QueryWrapper<>();
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole())) {
            List<Long> roomIds = managerScopeService.managedRoomIds(AuthUtils.getCurrentUserId());
            if (roomIds.isEmpty() || (roomId != null && !roomIds.contains(roomId))) return Result.success(List.of());
            if (roomId == null) qw.in("room_id", roomIds);
            else qw.eq("room_id", roomId);
        } else if (roomId != null) qw.eq("room_id", roomId);
        qw.orderByDesc("check_date");
        
        List<HygieneRecord> list = hygieneRecordService.list(qw);
        
        Map<Long, Room> roomMap = roomService.list().stream()
            .collect(Collectors.toMap(Room::getId, r -> r));
        Map<Long, String> buildingMap = buildingService.list().stream()
            .collect(Collectors.toMap(Building::getId, Building::getName));
        Map<Long, String> userMap = userService.list().stream()
            .collect(Collectors.toMap(User::getId, User::getName));
            
        for (HygieneRecord hr : list) {
            Room r = roomMap.get(hr.getRoomId());
            if (r != null) {
                String bName = buildingMap.getOrDefault(r.getBuildingId(), "");
                hr.setRoomName(bName + " " + r.getRoomNumber());
            }
            hr.setInspectorName(userMap.get(hr.getInspectorId()));
        }
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<HygieneRecord> getById(@PathVariable Long id) {
        HygieneRecord record = hygieneRecordService.getById(id);
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole()) && record != null
                && !managerScopeService.canManageRoom(AuthUtils.getCurrentUserId(), record.getRoomId())) {
            return Result.error(403, "无权查看该卫生记录");
        }
        return Result.success(record);
    }

    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody HygieneRecord hygieneRecord) {
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole()) && hygieneRecord.getId() != null) {
            HygieneRecord existing = hygieneRecordService.getById(hygieneRecord.getId());
            if (existing == null
                    || !managerScopeService.canManageRoom(AuthUtils.getCurrentUserId(), existing.getRoomId())) {
                return Result.error(403, "无权修改该卫生记录");
            }
        }
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole())
                && !managerScopeService.canManageRoom(AuthUtils.getCurrentUserId(), hygieneRecord.getRoomId())) {
            return Result.error(403, "无权修改该卫生记录");
        }
        return Result.success(hygieneRecordService.saveOrUpdate(hygieneRecord));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        HygieneRecord record = hygieneRecordService.getById(id);
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole()) && record != null
                && !managerScopeService.canManageRoom(AuthUtils.getCurrentUserId(), record.getRoomId())) {
            return Result.error(403, "无权删除该卫生记录");
        }
        return Result.success(hygieneRecordService.removeById(id));
    }
}
