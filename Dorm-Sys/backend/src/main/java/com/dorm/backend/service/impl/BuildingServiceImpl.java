package com.dorm.backend.service.impl;

import com.dorm.backend.entity.Building;
import com.dorm.backend.mapper.BuildingMapper;
import com.dorm.backend.service.BuildingService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.entity.Room;
import com.dorm.backend.entity.Bed;
import com.dorm.backend.service.RoomService;
import com.dorm.backend.service.BedService;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BuildingServiceImpl extends ServiceImpl<BuildingMapper, Building> implements BuildingService {
    
    @Autowired
    private RoomService roomService;
    
    @Autowired
    private BedService bedService;
    
    @Override
    public List<Building> getBuildingsWithStats(QueryWrapper<Building> bQw) {
        List<Building> buildings = this.list(bQw);
        List<Room> allRooms = roomService.list();
        List<Bed> allBeds = bedService.list();
        
        Map<Long, List<Room>> roomsByBuilding = allRooms.stream()
            .collect(Collectors.groupingBy(Room::getBuildingId));
            
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
        return buildings;
    }
}
