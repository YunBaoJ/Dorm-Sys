package com.dorm.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.entity.Bed;
import com.dorm.backend.entity.Building;
import com.dorm.backend.entity.FeeBill;
import com.dorm.backend.entity.Room;
import com.dorm.backend.mapper.FeeBillMapper;
import com.dorm.backend.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FeeBillServiceImpl extends ServiceImpl<FeeBillMapper, FeeBill> implements FeeBillService {

    private final RoomService roomService;
    private final BuildingService buildingService;
    private final BedService bedService;
    private final DormManagerScopeService managerScopeService;

    public FeeBillServiceImpl(RoomService roomService, BuildingService buildingService,
                              BedService bedService, DormManagerScopeService managerScopeService) {
        this.roomService = roomService;
        this.buildingService = buildingService;
        this.bedService = bedService;
        this.managerScopeService = managerScopeService;
    }

    @Override
    public List<FeeBill> listFeeBillsWithDetails(Long roomId, String status, String role, Long userId) {
        // 学生角色：自动定位到当前宿舍
        if ("student".equals(role) && userId != null) {
            Bed currentBed = bedService.getOne(new QueryWrapper<Bed>()
                    .eq("student_id", userId).last("LIMIT 1"));
            if (currentBed == null || currentBed.getRoomId() == null) {
                return List.of();
            }
            roomId = currentBed.getRoomId();
            // 学生只能查自己宿舍的账单，不必再用 status 之外的条件
            QueryWrapper<FeeBill> qw = new QueryWrapper<>();
            qw.eq("room_id", roomId);
            if (status != null && !status.isEmpty()) qw.eq("status", status);
            qw.orderByDesc("create_time");
            List<FeeBill> list = this.list(qw);
            enrichRoomNames(list);
            return list;
        }

        QueryWrapper<FeeBill> qw = new QueryWrapper<>();
        if (roomId != null) qw.eq("room_id", roomId);
        if (status != null && !status.isEmpty()) qw.eq("status", status);

        // 宿管角色：只看管辖楼栋
        if ("dormmanager".equals(role) && userId != null) {
            List<Long> roomIds = managerScopeService.managedRoomIds(userId);
            if (roomIds.isEmpty() || (roomId != null && !roomIds.contains(roomId))) return List.of();
            if (roomId == null) qw.in("room_id", roomIds);
        }

        qw.orderByDesc("create_time");
        List<FeeBill> list = this.list(qw);
        enrichRoomNames(list);
        return list;
    }

    @Override
    public FeeBill getFeeBillWithCheck(Long id, String role, Long userId) {
        FeeBill bill = this.getById(id);
        if (bill == null) return null;

        if ("student".equals(role)) {
            Bed currentBed = bedService.getOne(new QueryWrapper<Bed>()
                    .eq("student_id", userId).last("LIMIT 1"));
            if (currentBed == null || !bill.getRoomId().equals(currentBed.getRoomId())) {
                return null; // 调用方检查 null 并返回 403
            }
        } else if ("dormmanager".equals(role)) {
            if (!managerScopeService.canManageRoom(userId, bill.getRoomId())) {
                return null;
            }
        }
        return bill;
    }

    private void enrichRoomNames(List<FeeBill> bills) {
        if (bills.isEmpty()) return;
        // 只加载涉及到的房间和楼栋
        Set<Long> roomIds = bills.stream().map(FeeBill::getRoomId).collect(Collectors.toSet());
        Map<Long, Room> roomMap = roomIds.isEmpty() ? Collections.emptyMap()
                : roomService.listByIds(roomIds).stream().collect(Collectors.toMap(Room::getId, r -> r));

        Set<Long> buildingIds = roomMap.values().stream().map(Room::getBuildingId).collect(Collectors.toSet());
        Map<Long, String> buildingMap = buildingIds.isEmpty() ? Collections.emptyMap()
                : buildingService.listByIds(buildingIds).stream()
                    .collect(Collectors.toMap(Building::getId, Building::getName));

        for (FeeBill bill : bills) {
            Room r = roomMap.get(bill.getRoomId());
            if (r != null) {
                String bName = buildingMap.getOrDefault(r.getBuildingId(), "");
                bill.setRoomName(bName + " " + r.getRoomNumber());
            }
        }
    }
}
