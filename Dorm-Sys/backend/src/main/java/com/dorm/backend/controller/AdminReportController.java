package com.dorm.backend.controller;

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
import java.util.function.Function;
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
        List<Room> rooms = roomService.list();
        List<Bed> beds = bedService.list();
        List<RepairRequest> repairs = repairRequestService.list();
        Map<Long, Room> roomsById = rooms.stream().collect(Collectors.toMap(Room::getId, Function.identity()));
        long occupiedBeds = beds.stream().filter(item -> item.getStudentId() != null || "OCCUPIED".equals(item.getStatus())).count();
        long completedRepairs = repairs.stream().filter(item -> "COMPLETED".equals(item.getStatus())).count();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("buildingCount", buildings.size());
        data.put("roomCount", rooms.size());
        data.put("totalBeds", beds.size());
        data.put("occupiedBeds", occupiedBeds);
        data.put("emptyBeds", Math.max(0, beds.size() - occupiedBeds));
        data.put("occupancyRate", rate(occupiedBeds, beds.size()));
        data.put("repairCount", repairs.size());
        data.put("completedRepairs", completedRepairs);
        data.put("repairCompletionRate", rate(completedRepairs, repairs.size()));
        data.put("buildings", buildings.stream().map(building -> buildingRow(building, rooms, beds, roomsById)).toList());
        return Result.success(data);
    }

    private Map<String, Object> buildingRow(Building building, List<Room> rooms, List<Bed> beds, Map<Long, Room> roomsById) {
        List<Room> ownedRooms = rooms.stream().filter(item -> building.getId().equals(item.getBuildingId())).toList();
        long totalBeds = beds.stream().filter(item -> belongsToBuilding(item, building.getId(), roomsById)).count();
        long occupiedBeds = beds.stream().filter(item -> belongsToBuilding(item, building.getId(), roomsById))
                .filter(item -> item.getStudentId() != null || "OCCUPIED".equals(item.getStatus())).count();
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", building.getId()); row.put("name", building.getName()); row.put("roomCount", ownedRooms.size());
        row.put("totalBeds", totalBeds); row.put("occupiedBeds", occupiedBeds); row.put("occupancyRate", rate(occupiedBeds, totalBeds));
        return row;
    }

    private boolean belongsToBuilding(Bed bed, Long buildingId, Map<Long, Room> roomsById) {
        Room room = roomsById.get(bed.getRoomId());
        return room != null && buildingId.equals(room.getBuildingId());
    }

    private int rate(long numerator, long denominator) {
        return denominator == 0 ? 0 : (int) Math.round(numerator * 100.0 / denominator);
    }
}
