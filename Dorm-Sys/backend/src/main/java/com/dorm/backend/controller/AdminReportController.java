package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.AuthUtils;
import com.dorm.backend.common.Result;
import com.dorm.backend.entity.Bed;
import com.dorm.backend.entity.Building;
import com.dorm.backend.entity.RepairRequest;
import com.dorm.backend.entity.Room;
import com.dorm.backend.service.BedService;
import com.dorm.backend.service.BuildingService;
import com.dorm.backend.service.RepairRequestService;
import com.dorm.backend.service.RoomService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/report")
public class AdminReportController {
    private final BuildingService buildingService;
    private final RoomService roomService;
    private final BedService bedService;
    private final RepairRequestService repairRequestService;

    public AdminReportController(BuildingService buildingService, RoomService roomService, BedService bedService, RepairRequestService repairRequestService) {
        this.buildingService = buildingService;
        this.roomService = roomService;
        this.bedService = bedService;
        this.repairRequestService = repairRequestService;
    }

    @GetMapping("/summary")
    public Result<Map<String, Object>> summary() {
        if (!"admin".equals(AuthUtils.getCurrentUserRole())) return Result.error(403, "仅管理员可查看数据报表");
        List<Building> buildings = buildingService.list();
        long roomCount = roomService.count();
        long totalBeds = bedService.count();
        long occupiedBeds = bedService.count(new QueryWrapper<Bed>()
            .and(w -> w.isNotNull("student_id").or().eq("status", "OCCUPIED")));
        long repairCount = repairRequestService.count();
        long completedRepairs = repairRequestService.count(new QueryWrapper<RepairRequest>().eq("status", "COMPLETED"));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("buildingCount", (long) buildings.size());
        data.put("roomCount", roomCount);
        data.put("totalBeds", totalBeds);
        data.put("occupiedBeds", occupiedBeds);
        data.put("emptyBeds", Math.max(0, totalBeds - occupiedBeds));
        data.put("occupancyRate", rate(occupiedBeds, totalBeds));
        data.put("repairCount", repairCount);
        data.put("completedRepairs", completedRepairs);
        data.put("repairCompletionRate", rate(completedRepairs, repairCount));

        // Per-building data using targeted queries
        List<Long> buildingIds = buildings.stream().map(Building::getId).collect(Collectors.toList());
        List<Room> rooms = buildingIds.isEmpty() ? List.of() : roomService.list(new QueryWrapper<Room>().in("building_id", buildingIds));
        Map<Long, List<Room>> roomsByBuilding = rooms.stream().collect(Collectors.groupingBy(Room::getBuildingId));
        List<Long> roomIds = rooms.stream().map(Room::getId).collect(Collectors.toList());
        List<Bed> beds = roomIds.isEmpty() ? List.of() : bedService.list(new QueryWrapper<Bed>().in("room_id", roomIds));
        Map<Long, List<Bed>> bedsByRoom = beds.stream().collect(Collectors.groupingBy(Bed::getRoomId));

        data.put("buildings", buildings.stream().map(building -> buildingRow(building, roomsByBuilding, bedsByRoom)).toList());
        return Result.success(data);
    }

    private Map<String, Object> buildingRow(Building building, Map<Long, List<Room>> roomsByBuilding, Map<Long, List<Bed>> bedsByRoom) {
        List<Room> ownedRooms = roomsByBuilding.getOrDefault(building.getId(), List.of());
        long totalBeds = ownedRooms.stream()
            .flatMap(r -> bedsByRoom.getOrDefault(r.getId(), List.of()).stream())
            .count();
        long occupiedBeds = ownedRooms.stream()
            .flatMap(r -> bedsByRoom.getOrDefault(r.getId(), List.of()).stream())
            .filter(item -> item.getStudentId() != null || "OCCUPIED".equals(item.getStatus()))
            .count();
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", building.getId()); row.put("name", building.getName()); row.put("roomCount", ownedRooms.size());
        row.put("totalBeds", totalBeds); row.put("occupiedBeds", occupiedBeds); row.put("occupancyRate", rate(occupiedBeds, totalBeds));
        return row;
    }

    private int rate(long numerator, long denominator) {
        return denominator == 0 ? 0 : (int) Math.round(numerator * 100.0 / denominator);
    }
}
