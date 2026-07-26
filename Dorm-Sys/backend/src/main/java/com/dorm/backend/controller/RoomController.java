package com.dorm.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.common.AuthUtils;
import com.dorm.backend.entity.Room;
import com.dorm.backend.entity.Bed;
import com.dorm.backend.entity.Building;
import com.dorm.backend.dto.RoomBatchCreateRequest;
import com.dorm.backend.service.RoomService;
import com.dorm.backend.service.BuildingService;
import com.dorm.backend.service.BedService;
import com.dorm.backend.service.DormManagerScopeService;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/room")
public class RoomController {

    private final RoomService roomService;
    private final BuildingService buildingService;
    private final BedService bedService;
    private final DormManagerScopeService managerScopeService;

    public RoomController(RoomService roomService, BuildingService buildingService,
                          BedService bedService, DormManagerScopeService managerScopeService) {
        this.roomService = roomService;
        this.buildingService = buildingService;
        this.bedService = bedService;
        this.managerScopeService = managerScopeService;
    }

    @GetMapping("/list")
    public Result<List<Room>> list(@RequestParam(required = false) Long buildingId,
                                   @RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "100") Integer size) {
        List<Room> rooms = roomService.listRoomsWithDetails(buildingId,
                AuthUtils.getCurrentUserRole(), AuthUtils.getCurrentUserId());
        return Result.success(rooms);
    }

    @GetMapping("/{id}")
    public Result<Room> getById(@PathVariable Long id) {
        Room room = roomService.getById(id);
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole()) && room != null
                && !managerScopeService.canManageBuilding(AuthUtils.getCurrentUserId(), room.getBuildingId())) {
            return Result.error(403, "无权查看该房间");
        }
        return Result.success(room);
    }

    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody Room room) {
        boolean isNew = room.getId() == null;
        boolean result = roomService.saveOrUpdate(room);
        
        // Auto-generate beds if new room
        if (isNew && result && room.getCapacity() != null && room.getCapacity() > 0) {
            for (int i = 1; i <= room.getCapacity(); i++) {
                Bed bed = new Bed();
                bed.setRoomId(room.getId());
                bed.setBedNumber(room.getRoomNumber() + "-" + i);
                bed.setStatus("EMPTY");
                bedService.save(bed);
            }
        }
        return Result.success(result);
    }

    @PostMapping("/batch")
    @Transactional
    public Result<Integer> batchCreate(@RequestBody RoomBatchCreateRequest request) {
        if (request.getBuildingId() == null || request.getStartFloor() == null
                || request.getEndFloor() == null || request.getRoomsPerFloor() == null
                || request.getStartSequence() == null || request.getCapacity() == null) {
            return Result.error(400, "请完整填写批量创建参数");
        }

        Building building = buildingService.getById(request.getBuildingId());
        if (building == null) return Result.error(400, "所选楼栋不存在");
        if (request.getStartFloor() < 1 || request.getEndFloor() < request.getStartFloor()
                || building.getFloors() == null || request.getEndFloor() > building.getFloors()) {
            return Result.error(400, "楼层范围超出楼栋实际楼层");
        }
        if (request.getRoomsPerFloor() < 1 || request.getStartSequence() < 1
                || request.getStartSequence() + request.getRoomsPerFloor() - 1 > 99) {
            return Result.error(400, "每层房间序号必须在 01 至 99 之间");
        }
        if (request.getCapacity() < 1 || request.getCapacity() > 12) {
            return Result.error(400, "房间容量必须在 1 至 12 人之间");
        }

        int roomCount = (request.getEndFloor() - request.getStartFloor() + 1)
                * request.getRoomsPerFloor();
        if (roomCount > 500) return Result.error(400, "单次最多创建 500 个房间");

        List<Room> rooms = new ArrayList<>(roomCount);
        for (int floor = request.getStartFloor(); floor <= request.getEndFloor(); floor++) {
            for (int offset = 0; offset < request.getRoomsPerFloor(); offset++) {
                Room room = new Room();
                room.setBuildingId(request.getBuildingId());
                room.setRoomNumber(String.format("%d%02d", floor, request.getStartSequence() + offset));
                room.setFloor(floor);
                room.setCapacity(request.getCapacity());
                room.setStatus("NORMAL");
                rooms.add(room);
            }
        }

        List<String> roomNumbers = rooms.stream().map(Room::getRoomNumber).toList();
        List<Room> existingRooms = roomService.list(new QueryWrapper<Room>()
                .eq("building_id", request.getBuildingId())
                .in("room_number", roomNumbers));
        if (!existingRooms.isEmpty()) {
            Set<String> existingNumbers = existingRooms.stream()
                    .map(Room::getRoomNumber).collect(Collectors.toSet());
            String conflict = roomNumbers.stream().filter(existingNumbers::contains).findFirst().orElse("");
            return Result.error(400, "房间号已存在：" + conflict);
        }

        if (!roomService.saveBatch(rooms)) throw new IllegalStateException("批量创建房间失败");

        List<Bed> beds = new ArrayList<>(roomCount * request.getCapacity());
        for (Room room : rooms) {
            if (room.getId() == null) throw new IllegalStateException("批量创建房间未返回编号");
            for (int index = 1; index <= room.getCapacity(); index++) {
                Bed bed = new Bed();
                bed.setRoomId(room.getId());
                bed.setBedNumber(room.getRoomNumber() + "-" + index);
                bed.setStatus("EMPTY");
                beds.add(bed);
            }
        }
        if (!beds.isEmpty() && !bedService.saveBatch(beds)) {
            throw new IllegalStateException("批量创建床位失败");
        }
        return Result.success(roomCount);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        // Delete associated beds first
        QueryWrapper<Bed> bedQuery = new QueryWrapper<>();
        bedQuery.eq("room_id", id);
        bedService.remove(bedQuery);
        
        return Result.success(roomService.removeById(id));
    }
}
