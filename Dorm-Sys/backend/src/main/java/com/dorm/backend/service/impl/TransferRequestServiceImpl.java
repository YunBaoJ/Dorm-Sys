package com.dorm.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.entity.*;
import com.dorm.backend.mapper.TransferRequestMapper;
import com.dorm.backend.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransferRequestServiceImpl extends ServiceImpl<TransferRequestMapper, TransferRequest> implements TransferRequestService {

    private final UserService userService;
    private final BedService bedService;
    private final RoomService roomService;
    private final BuildingService buildingService;
    private final DormManagerScopeService managerScopeService;

    public TransferRequestServiceImpl(UserService userService, BedService bedService,
                                      RoomService roomService, BuildingService buildingService,
                                      DormManagerScopeService managerScopeService) {
        this.userService = userService;
        this.bedService = bedService;
        this.roomService = roomService;
        this.buildingService = buildingService;
        this.managerScopeService = managerScopeService;
    }

    @Override
    public List<TransferRequest> listTransferRequestsWithDetails(Long studentId, String status, String role, Long userId) {
        QueryWrapper<TransferRequest> queryWrapper = new QueryWrapper<>();
        if ("student".equals(role)) studentId = userId;
        if (studentId != null) queryWrapper.eq("student_id", studentId);
        if (status != null && !status.isEmpty()) queryWrapper.eq("status", status);
        queryWrapper.orderByDesc("create_time");

        List<TransferRequest> list = this.list(queryWrapper);
        if (list.isEmpty()) return list;

        // 按需加载关联数据
        Set<Long> studentIds = list.stream().map(TransferRequest::getStudentId).collect(Collectors.toSet());
        Set<Long> bedIds = list.stream().map(TransferRequest::getCurrentBedId).collect(Collectors.toSet());
        Set<Long> targetRoomIds = list.stream().map(TransferRequest::getTargetRoomId)
                .filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, String> userMap = studentIds.isEmpty() ? Collections.emptyMap()
                : userService.listByIds(studentIds).stream().collect(Collectors.toMap(User::getId, User::getName));
        Map<Long, Bed> bedMap = bedIds.isEmpty() ? Collections.emptyMap()
                : bedService.listByIds(bedIds).stream().collect(Collectors.toMap(Bed::getId, b -> b));

        Set<Long> allRoomIds = new HashSet<>(targetRoomIds);
        bedMap.values().stream().map(Bed::getRoomId).filter(Objects::nonNull).forEach(allRoomIds::add);
        Map<Long, Room> roomMap = allRoomIds.isEmpty() ? Collections.emptyMap()
                : roomService.listByIds(allRoomIds).stream().collect(Collectors.toMap(Room::getId, r -> r));

        Set<Long> buildingIds = roomMap.values().stream().map(Room::getBuildingId).collect(Collectors.toSet());
        Map<Long, String> buildingMap = buildingIds.isEmpty() ? Collections.emptyMap()
                : buildingService.listByIds(buildingIds).stream()
                    .collect(Collectors.toMap(Building::getId, Building::getName));

        // 宿管过滤：只看管辖楼栋的调宿申请
        if ("dormmanager".equals(role)) {
            List<Long> managedRoomIds = managerScopeService.managedRoomIds(userId);
            Set<Long> managedSet = new HashSet<>(managedRoomIds);
            list = list.stream().filter(req -> {
                Bed currentBed = bedMap.get(req.getCurrentBedId());
                return currentBed != null && managedSet.contains(currentBed.getRoomId());
            }).collect(Collectors.toList());
        }

        for (TransferRequest req : list) {
            req.setStudentName(userMap.get(req.getStudentId()));
            Bed currentBed = bedMap.get(req.getCurrentBedId());
            if (currentBed != null) {
                Room r = roomMap.get(currentBed.getRoomId());
                if (r != null) {
                    String bName = buildingMap.get(r.getBuildingId());
                    req.setCurrentBedName(bName + " " + r.getRoomNumber() + " - " + currentBed.getBedNumber());
                }
            }
            if (req.getTargetRoomId() != null) {
                Room r = roomMap.get(req.getTargetRoomId());
                if (r != null) {
                    String bName = buildingMap.get(r.getBuildingId());
                    req.setTargetRoomName(bName + " " + r.getRoomNumber());
                }
            }
        }
        return list;
    }
}
