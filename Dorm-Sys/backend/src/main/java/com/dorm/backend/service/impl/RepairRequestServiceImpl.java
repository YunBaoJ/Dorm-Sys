package com.dorm.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.entity.*;
import com.dorm.backend.mapper.RepairRequestMapper;
import com.dorm.backend.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RepairRequestServiceImpl extends ServiceImpl<RepairRequestMapper, RepairRequest> implements RepairRequestService {

    private final UserService userService;
    private final RoomService roomService;
    private final BuildingService buildingService;
    private final BedService bedService;
    private final DormManagerScopeService managerScopeService;

    public RepairRequestServiceImpl(UserService userService, RoomService roomService,
                                    BuildingService buildingService, BedService bedService,
                                    DormManagerScopeService managerScopeService) {
        this.userService = userService;
        this.roomService = roomService;
        this.buildingService = buildingService;
        this.bedService = bedService;
        this.managerScopeService = managerScopeService;
    }

    @Override
    public List<RepairRequest> listRepairRequestsWithDetails(Long submitterId, String status, String role, Long userId) {
        QueryWrapper<RepairRequest> queryWrapper = new QueryWrapper<>();

        // 学生只能看自己的报修
        if ("student".equals(role)) submitterId = userId;
        // 宿管只看管辖楼栋的报修
        if ("dormmanager".equals(role)) {
            List<Long> roomIds = managerScopeService.managedRoomIds(userId);
            if (roomIds.isEmpty()) return List.of();
            queryWrapper.in("room_id", roomIds);
        }
        if (submitterId != null) queryWrapper.eq("submitter_id", submitterId);
        if (status != null && !status.isEmpty()) queryWrapper.eq("status", status);
        queryWrapper.orderByDesc("create_time");

        List<RepairRequest> list = this.list(queryWrapper);
        if (list.isEmpty()) return list;

        // 收集所有需要关联的用户/房间ID
        Set<Long> userIds = list.stream()
                .map(r -> r.getSubmitterId())
                .collect(Collectors.toSet());
        list.stream().map(RepairRequest::getHandlerId).filter(java.util.Objects::nonNull).forEach(userIds::add);

        Set<Long> roomIds = list.stream()
                .map(RepairRequest::getRoomId)
                .collect(Collectors.toSet());

        // 按需加载数据
        Map<Long, String> userMap = userIds.isEmpty() ? Collections.emptyMap()
                : userService.listByIds(userIds).stream()
                    .collect(Collectors.toMap(User::getId, User::getName));

        Map<Long, Room> roomMap = roomIds.isEmpty() ? Collections.emptyMap()
                : roomService.listByIds(roomIds).stream()
                    .collect(Collectors.toMap(Room::getId, r -> r));

        Set<Long> buildingIds = roomMap.values().stream().map(Room::getBuildingId).collect(Collectors.toSet());
        Map<Long, String> buildingMap = buildingIds.isEmpty() ? Collections.emptyMap()
                : buildingService.listByIds(buildingIds).stream()
                    .collect(Collectors.toMap(Building::getId, Building::getName));

        // 填充显示名称
        for (RepairRequest req : list) {
            req.setSubmitterName(userMap.get(req.getSubmitterId()));
            if (req.getHandlerId() != null) req.setHandlerName(userMap.get(req.getHandlerId()));
            Room r = roomMap.get(req.getRoomId());
            if (r != null) {
                String bName = buildingMap.get(r.getBuildingId());
                req.setRoomName((bName != null ? bName : "") + " " + r.getRoomNumber());
            }
        }
        return list;
    }
}
