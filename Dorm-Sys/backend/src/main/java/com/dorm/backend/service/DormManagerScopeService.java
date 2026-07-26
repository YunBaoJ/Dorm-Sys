package com.dorm.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.entity.Building;
import com.dorm.backend.entity.ManagerInfo;
import com.dorm.backend.entity.Room;
import com.dorm.backend.entity.User;
import com.dorm.backend.entity.Bed;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DormManagerScopeService {
    private final ManagerInfoService managerInfoService;
    private final UserService userService;
    private final BuildingService buildingService;
    private final RoomService roomService;
    private final BedService bedService;

    // Simple in-memory cache with TTL (5 seconds) to avoid repeated DB queries within short intervals
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 5000;

    public DormManagerScopeService(ManagerInfoService managerInfoService, UserService userService,
                                   @Lazy BuildingService buildingService, @Lazy RoomService roomService, @Lazy BedService bedService) {
        this.managerInfoService = managerInfoService;
        this.userService = userService;
        this.buildingService = buildingService;
        this.roomService = roomService;
        this.bedService = bedService;
    }

    @SuppressWarnings("unchecked")
    private <T> T getCached(String key) {
        CacheEntry entry = cache.get(key);
        if (entry != null && !entry.isExpired()) {
            return (T) entry.value;
        }
        cache.remove(key);
        return null;
    }

    private void putCache(String key, Object value) {
        cache.put(key, new CacheEntry(value, CACHE_TTL_MS));
    }

    public List<Long> managedBuildingIds(Long managerUserId) {
        if (managerUserId == null) return List.of();
        String key = "buildingIds:" + managerUserId;
        List<Long> cached = getCached(key);
        if (cached != null) return cached;

        List<Long> assignedIds = managerInfoService.list(new QueryWrapper<ManagerInfo>()
                .eq("user_id", managerUserId)).stream()
            .map(ManagerInfo::getBuildingId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        if (!assignedIds.isEmpty()) {
            putCache(key, assignedIds);
            return assignedIds;
        }

        User manager = userService.getById(managerUserId);
        if (manager == null || manager.getName() == null || manager.getName().isBlank()) return List.of();
        List<Long> result = buildingService.list(new QueryWrapper<Building>().eq("manager", manager.getName())).stream()
            .map(Building::getId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        putCache(key, result);
        return result;
    }

    public List<Long> managedRoomIds(Long managerUserId) {
        if (managerUserId == null) return List.of();
        String key = "roomIds:" + managerUserId;
        List<Long> cached = getCached(key);
        if (cached != null) return cached;

        List<Long> buildingIds = managedBuildingIds(managerUserId);
        if (buildingIds.isEmpty()) return List.of();
        List<Long> result = roomService.list(new QueryWrapper<Room>().in("building_id", buildingIds)).stream()
            .map(Room::getId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        putCache(key, result);
        return result;
    }

    public boolean canManageBuilding(Long managerUserId, Long buildingId) {
        return buildingId != null && managedBuildingIds(managerUserId).contains(buildingId);
    }

    public List<String> managedBuildingNames(Long managerUserId) {
        if (managerUserId == null) return List.of();
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
        if (managerUserId == null) return List.of();
        String key = "studentIds:" + managerUserId;
        List<Long> cached = getCached(key);
        if (cached != null) return cached;

        List<Long> roomIds = managedRoomIds(managerUserId);
        if (roomIds.isEmpty()) return List.of();
        List<Long> result = bedService.list(new QueryWrapper<Bed>().in("room_id", roomIds).isNotNull("student_id")).stream()
            .map(Bed::getStudentId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        putCache(key, result);
        return result;
    }

    public boolean canManageStudent(Long managerUserId, Long studentId) {
        return studentId != null && managedStudentIds(managerUserId).contains(studentId);
    }

    private static class CacheEntry {
        final Object value;
        final long expireAt;
        CacheEntry(Object value, long ttlMs) {
            this.value = value;
            this.expireAt = System.currentTimeMillis() + ttlMs;
        }
        boolean isExpired() { return System.currentTimeMillis() > expireAt; }
    }
}
