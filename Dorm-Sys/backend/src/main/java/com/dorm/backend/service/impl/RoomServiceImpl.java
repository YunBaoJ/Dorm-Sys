package com.dorm.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.entity.Bed;
import com.dorm.backend.entity.Building;
import com.dorm.backend.entity.Room;
import com.dorm.backend.mapper.RoomMapper;
import com.dorm.backend.service.BedService;
import com.dorm.backend.service.BuildingService;
import com.dorm.backend.service.DormManagerScopeService;
import com.dorm.backend.service.RoomService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> implements RoomService {

    private final BuildingService buildingService;
    private final BedService bedService;
    private final DormManagerScopeService managerScopeService;

    public RoomServiceImpl(BuildingService buildingService, BedService bedService,
                           DormManagerScopeService managerScopeService) {
        this.buildingService = buildingService;
        this.bedService = bedService;
        this.managerScopeService = managerScopeService;
    }

    @Override
    public List<Room> listRoomsWithDetails(Long buildingId, String role, Long userId) {
        QueryWrapper<Room> queryWrapper = new QueryWrapper<>();

        // 角色数据隔离：宿管只看管辖楼栋
        if ("dormmanager".equals(role)) {
            List<Long> buildingIds = managerScopeService.managedBuildingIds(userId);
            if (buildingIds.isEmpty() || (buildingId != null && !buildingIds.contains(buildingId))) {
                return List.of();
            }
            if (buildingId == null) queryWrapper.in("building_id", buildingIds);
            else queryWrapper.eq("building_id", buildingId);
        } else if (buildingId != null) {
            queryWrapper.eq("building_id", buildingId);
        }

        List<Room> rooms = this.list(queryWrapper);
        if (rooms.isEmpty()) return rooms;

        // 按需加载楼栋名称：只加载涉及到的楼栋
        Set<Long> buildingIds = rooms.stream().map(Room::getBuildingId).collect(Collectors.toSet());
        Map<Long, String> buildingMap;
        if (buildingIds.isEmpty()) {
            buildingMap = Collections.emptyMap();
        } else {
            buildingMap = buildingService.listByIds(buildingIds).stream()
                    .collect(Collectors.toMap(Building::getId, Building::getName));
        }

        // 按需统计每间宿舍的入住人数：使用 SQL COUNT 而非全表加载
        for (Room room : rooms) {
            room.setBuildingName(buildingMap.get(room.getBuildingId()));
            long occupied = bedService.count(new QueryWrapper<Bed>()
                    .eq("room_id", room.getId())
                    .and(w -> w.isNotNull("student_id").or().eq("status", "OCCUPIED")));
            room.setOccupied((int) occupied);
        }
        return rooms;
    }
}
