package com.dorm.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.entity.*;
import com.dorm.backend.mapper.HygieneRecordMapper;
import com.dorm.backend.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class HygieneRecordServiceImpl extends ServiceImpl<HygieneRecordMapper, HygieneRecord> implements HygieneRecordService {

    private final RoomService roomService;
    private final BuildingService buildingService;
    private final UserService userService;
    private final DormManagerScopeService managerScopeService;

    public HygieneRecordServiceImpl(RoomService roomService, BuildingService buildingService,
                                    UserService userService, DormManagerScopeService managerScopeService) {
        this.roomService = roomService;
        this.buildingService = buildingService;
        this.userService = userService;
        this.managerScopeService = managerScopeService;
    }

    @Override
    public List<HygieneRecord> listHygieneRecordsWithDetails(Long roomId, String role, Long userId) {
        QueryWrapper<HygieneRecord> qw = new QueryWrapper<>();
        if ("dormmanager".equals(role)) {
            List<Long> roomIds = managerScopeService.managedRoomIds(userId);
            if (roomIds.isEmpty() || (roomId != null && !roomIds.contains(roomId))) return List.of();
            if (roomId == null) qw.in("room_id", roomIds);
            else qw.eq("room_id", roomId);
        } else if (roomId != null) qw.eq("room_id", roomId);
        qw.orderByDesc("check_date");

        List<HygieneRecord> list = this.list(qw);
        if (list.isEmpty()) return list;

        // 按需加载关联数据
        Set<Long> roomIds = list.stream().map(HygieneRecord::getRoomId).collect(Collectors.toSet());
        Set<Long> inspectorIds = list.stream().map(HygieneRecord::getInspectorId).collect(Collectors.toSet());

        Map<Long, Room> roomMap = roomIds.isEmpty() ? Collections.emptyMap()
                : roomService.listByIds(roomIds).stream().collect(Collectors.toMap(Room::getId, r -> r));
        Map<Long, String> buildingMap = roomMap.isEmpty() ? Collections.emptyMap()
                : buildingService.listByIds(roomMap.values().stream().map(Room::getBuildingId).collect(Collectors.toSet()))
                    .stream().collect(Collectors.toMap(Building::getId, Building::getName));
        Map<Long, String> userMap = inspectorIds.isEmpty() ? Collections.emptyMap()
                : userService.listByIds(inspectorIds).stream().collect(Collectors.toMap(User::getId, User::getName));

        for (HygieneRecord hr : list) {
            Room r = roomMap.get(hr.getRoomId());
            if (r != null) {
                String bName = buildingMap.getOrDefault(r.getBuildingId(), "");
                hr.setRoomName(bName + " " + r.getRoomNumber());
            }
            hr.setInspectorName(userMap.get(hr.getInspectorId()));
        }
        return list;
    }
}
