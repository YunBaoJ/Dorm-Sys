package com.dorm.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.entity.Building;
import com.dorm.backend.entity.ManagerInfo;
import com.dorm.backend.entity.Room;
import com.dorm.backend.entity.User;
import com.dorm.backend.entity.Bed;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class DormManagerScopeService {
    private final ManagerInfoService managerInfoService;
    private final UserService userService;
    private final BuildingService buildingService;
    private final RoomService roomService;
    private final BedService bedService;

    public DormManagerScopeService(ManagerInfoService managerInfoService, UserService userService,
                                   BuildingService buildingService, RoomService roomService, BedService bedService) {
        this.managerInfoService = managerInfoService;
        this.userService = userService;
        this.buildingService = buildingService;
        this.roomService = roomService;
        this.bedService = bedService;
    }

    public List<Long> managedBuildingIds(Long managerUserId) {
        if (managerUserId == null) return List.of();
        List<Long> assignedIds = managerInfoService.list(new QueryWrapper<ManagerInfo>()
                .eq("user_id", managerUserId)).stream()
            .map(ManagerInfo::getBuildingId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        if (!assignedIds.isEmpty()) return assignedIds;

        User manager = userService.getById(managerUserId);
        if (manager == null || manager.getName() == null || manager.getName().isBlank()) return List.of();
        return buildingService.list(new QueryWrapper<Building>().eq("manager", manager.getName())).stream()
            .map(Building::getId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    }

    public List<Long> managedRoomIds(Long managerUserId) {
        List<Long> buildingIds = managedBuildingIds(managerUserId);
        if (buildingIds.isEmpty()) return List.of();
        return roomService.list(new QueryWrapper<Room>().in("building_id", buildingIds)).stream()
            .map(Room::getId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    }

    public boolean canManageBuilding(Long managerUserId, Long buildingId) {
        return buildingId != null && managedBuildingIds(managerUserId).contains(buildingId);
    }

    public List<String> managedBuildingNames(Long managerUserId) {
        List<Long> buildingIds = managedBuildingIds(managerUserId);
        if (buildingIds.isEmpty()) return List.of();
        return buildingService.list(new QueryWrapper<Building>().in("id", buildingIds)).stream()
            .map(Building::getName)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    }

    public boolean canManageRoom(Long managerUserId, Long roomId) {
        return roomId != null && managedRoomIds(managerUserId).contains(roomId);
    }

    public List<Long> managedStudentIds(Long managerUserId) {
        List<Long> roomIds = managedRoomIds(managerUserId);
        if (roomIds.isEmpty()) return List.of();
        return bedService.list(new QueryWrapper<Bed>().in("room_id", roomIds).isNotNull("student_id")).stream()
            .map(Bed::getStudentId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    }

    public boolean canManageStudent(Long managerUserId, Long studentId) {
        return studentId != null && managedStudentIds(managerUserId).contains(studentId);
    }
}
