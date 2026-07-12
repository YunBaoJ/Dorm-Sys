package com.dorm.backend.controller;

import com.dorm.backend.common.Result;
import com.dorm.backend.entity.Building;
import com.dorm.backend.entity.Room;
import com.dorm.backend.entity.Bed;
import com.dorm.backend.service.BuildingService;
import com.dorm.backend.service.RoomService;
import com.dorm.backend.service.BedService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

@RestController
@RequestMapping("/api/building")
public class BuildingController {

    @Autowired
    private BuildingService buildingService;
    
    @Autowired
    private RoomService roomService;
    
    @Autowired
    private BedService bedService;

    @GetMapping("/list")
    public Result<List<Building>> list() {
        List<Building> buildings = buildingService.list();
        List<Room> allRooms = roomService.list();
        List<Bed> allBeds = bedService.list();
        
        // Group rooms by buildingId
        Map<Long, List<Room>> roomsByBuilding = allRooms.stream()
            .collect(Collectors.groupingBy(Room::getBuildingId));
            
        // Group beds by roomId
        Map<Long, List<Bed>> bedsByRoom = allBeds.stream()
            .collect(Collectors.groupingBy(Bed::getRoomId));

        for (Building b : buildings) {
            List<Room> bRooms = roomsByBuilding.get(b.getId());
            if (bRooms == null || bRooms.isEmpty()) {
                b.setTotalRooms(0);
                b.setOccupiedRooms(0);
                b.setFreeRooms(0);
                continue;
            }
            int total = bRooms.size();
            int occupied = 0;
            for (Room r : bRooms) {
                List<Bed> rBeds = bedsByRoom.get(r.getId());
                boolean isRoomOccupied = false;
                if (rBeds != null) {
                    for (Bed bed : rBeds) {
                        if (bed.getStudentId() != null) {
                            isRoomOccupied = true;
                            break;
                        }
                    }
                }
                if (isRoomOccupied) occupied++;
            }
            b.setTotalRooms(total);
            b.setOccupiedRooms(occupied);
            b.setFreeRooms(total - occupied);
        }

        return Result.success(buildings);
    }

    @GetMapping("/{id}")
    public Result<Building> getById(@PathVariable Long id) {
        return Result.success(buildingService.getById(id));
    }

    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody Building building) {
        return Result.success(buildingService.saveOrUpdate(building));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(buildingService.removeById(id));
    }
}
