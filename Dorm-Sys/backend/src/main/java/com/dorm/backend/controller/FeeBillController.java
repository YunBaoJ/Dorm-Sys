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

@RestController
@RequestMapping("/api/feeBill")
public class FeeBillController {

    @Autowired
    private FeeBillService feeBillService;
    
    @Autowired
    private RoomService roomService;
    
    @Autowired
    private BuildingService buildingService;

    @GetMapping("/list")
    public Result<List<FeeBill>> list(@RequestParam(required = false) Long roomId,
                                      @RequestParam(required = false) String status) {
        QueryWrapper<FeeBill> qw = new QueryWrapper<>();
        if (roomId != null) qw.eq("room_id", roomId);
        if (status != null && !status.isEmpty()) qw.eq("status", status);
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
        return Result.success(feeBillService.getById(id));
    }

    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody FeeBill feeBill) {
        return Result.success(feeBillService.saveOrUpdate(feeBill));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(feeBillService.removeById(id));
    }
}
