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

    @GetMapping("/list")
    public Result<List<HygieneRecord>> list(@RequestParam(required = false) Long roomId) {
        QueryWrapper<HygieneRecord> qw = new QueryWrapper<>();
        if (roomId != null) qw.eq("room_id", roomId);
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
        return Result.success(hygieneRecordService.getById(id));
    }

    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody HygieneRecord hygieneRecord) {
        return Result.success(hygieneRecordService.saveOrUpdate(hygieneRecord));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(hygieneRecordService.removeById(id));
    }
}
