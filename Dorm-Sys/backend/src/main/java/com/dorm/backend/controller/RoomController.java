package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.entity.Room;
import com.dorm.backend.entity.Bed;
import com.dorm.backend.entity.Building;
import com.dorm.backend.service.RoomService;
import com.dorm.backend.service.BuildingService;
import com.dorm.backend.service.BedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/room")
public class RoomController {

    @Autowired
    private RoomService roomService;
    
    @Autowired
    private BuildingService buildingService;
    
    @Autowired
    private BedService bedService;

    @GetMapping("/list")
    public Result<List<Room>> list(@RequestParam(required = false) Long buildingId) {
        QueryWrapper<Room> queryWrapper = new QueryWrapper<>();
        if (buildingId != null) {
            queryWrapper.eq("building_id", buildingId);
        }
        List<Room> rooms = roomService.list(queryWrapper);
        
        // Populate building names
        List<Building> buildings = buildingService.list();
        Map<Long, String> buildingMap = buildings.stream()
            .collect(Collectors.toMap(Building::getId, Building::getName));
            
        // Populate occupied beds
        List<Bed> allBeds = bedService.list();
        Map<Long, Long> occupiedCountMap = allBeds.stream()
            .filter(b -> b.getStudentId() != null || "OCCUPIED".equals(b.getStatus()))
            .collect(Collectors.groupingBy(Bed::getRoomId, Collectors.counting()));
            
        for (Room room : rooms) {
            room.setBuildingName(buildingMap.get(room.getBuildingId()));
            room.setOccupied(occupiedCountMap.getOrDefault(room.getId(), 0L).intValue());
        }
        
        return Result.success(rooms);
    }

    @GetMapping("/{id}")
    public Result<Room> getById(@PathVariable Long id) {
        return Result.success(roomService.getById(id));
    }

    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody Room room) {
        boolean isNew = room.getId() == null;
        boolean result = roomService.saveOrUpdate(room);
        
        // Auto-generate beds if new room
        if (isNew && result && room.getCapacity() != null && room.getCapacity() > 0) {
            for (int i = 1; i <= room.getCapacity(); i++) {
                Bed bed = new Bed();
                bed.setRoomId(room.getId());
                bed.setBedNumber(room.getRoomNumber() + "-" + i);
                bed.setStatus("EMPTY");
                bedService.save(bed);
            }
        }
        return Result.success(result);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        // Delete associated beds first
        QueryWrapper<Bed> bedQuery = new QueryWrapper<>();
        bedQuery.eq("room_id", id);
        bedService.remove(bedQuery);
        
        return Result.success(roomService.removeById(id));
    }
}
