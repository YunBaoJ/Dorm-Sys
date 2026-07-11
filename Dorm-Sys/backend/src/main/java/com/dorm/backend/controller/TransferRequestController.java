package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.entity.TransferRequest;
import com.dorm.backend.entity.User;
import com.dorm.backend.entity.Bed;
import com.dorm.backend.entity.Room;
import com.dorm.backend.entity.Building;
import com.dorm.backend.service.TransferRequestService;
import com.dorm.backend.service.UserService;
import com.dorm.backend.service.BedService;
import com.dorm.backend.service.RoomService;
import com.dorm.backend.service.BuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transferRequest")
public class TransferRequestController {

    @Autowired
    private TransferRequestService transferRequestService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private BedService bedService;
    
    @Autowired
    private RoomService roomService;
    
    @Autowired
    private BuildingService buildingService;

    @GetMapping("/list")
    public Result<List<TransferRequest>> list(@RequestParam(required = false) Long studentId,
                                              @RequestParam(required = false) String status) {
        QueryWrapper<TransferRequest> queryWrapper = new QueryWrapper<>();
        if (studentId != null) queryWrapper.eq("student_id", studentId);
        if (status != null && !status.isEmpty()) queryWrapper.eq("status", status);
        
        queryWrapper.orderByDesc("create_time");
        List<TransferRequest> list = transferRequestService.list(queryWrapper);
        
        Map<Long, String> userMap = userService.list().stream()
            .collect(Collectors.toMap(User::getId, User::getName));
            
        Map<Long, Bed> bedMap = bedService.list().stream()
            .collect(Collectors.toMap(Bed::getId, b -> b));
            
        Map<Long, Room> roomMap = roomService.list().stream()
            .collect(Collectors.toMap(Room::getId, r -> r));
            
        Map<Long, String> buildingMap = buildingService.list().stream()
            .collect(Collectors.toMap(Building::getId, Building::getName));
            
        for (TransferRequest req : list) {
            req.setStudentName(userMap.get(req.getStudentId()));
            
            // Format current bed string
            Bed currentBed = bedMap.get(req.getCurrentBedId());
            if (currentBed != null) {
                Room r = roomMap.get(currentBed.getRoomId());
                if (r != null) {
                    String bName = buildingMap.get(r.getBuildingId());
                    req.setCurrentBedName(bName + " " + r.getRoomNumber() + " - " + currentBed.getBedNumber());
                }
            }
            
            // Format target room string if specified
            if (req.getTargetRoomId() != null) {
                Room r = roomMap.get(req.getTargetRoomId());
                if (r != null) {
                    String bName = buildingMap.get(r.getBuildingId());
                    req.setTargetRoomName(bName + " " + r.getRoomNumber());
                }
            }
        }
        
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<TransferRequest> getById(@PathVariable Long id) {
        return Result.success(transferRequestService.getById(id));
    }

    @PostMapping("/save")
    @Transactional
    public Result<Boolean> save(@RequestBody TransferRequest transferRequest) {
        if ("APPROVED".equals(transferRequest.getStatus())) {
            Result<Boolean> transferResult = applyApprovedTransfer(transferRequest);
            if (transferResult.getCode() != 200) {
                return transferResult;
            }
        }
        return Result.success(transferRequestService.saveOrUpdate(transferRequest));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(transferRequestService.removeById(id));
    }

    private Result<Boolean> applyApprovedTransfer(TransferRequest transferRequest) {
        if (transferRequest.getStudentId() == null) {
            return Result.error(400, "申请学生不能为空");
        }
        if (transferRequest.getTargetRoomId() == null) {
            return Result.error(400, "批准调宿时必须指定目标房间");
        }

        Bed currentBed = findCurrentBed(transferRequest);
        if (currentBed != null && transferRequest.getTargetRoomId().equals(currentBed.getRoomId())) {
            return Result.success(true);
        }

        Bed targetBed = findAvailableTargetBed(transferRequest.getTargetRoomId());
        if (targetBed == null) {
            return Result.error(400, "目标房间暂无可用床位");
        }

        if (currentBed != null && !currentBed.getId().equals(targetBed.getId())) {
            releaseBed(currentBed);
        }

        targetBed.setStudentId(transferRequest.getStudentId());
        targetBed.setStatus("OCCUPIED");
        bedService.updateById(targetBed);
        transferRequest.setCurrentBedId(currentBed != null ? currentBed.getId() : transferRequest.getCurrentBedId());

        refreshRoomStatus(currentBed != null ? currentBed.getRoomId() : null);
        refreshRoomStatus(targetBed.getRoomId());
        return Result.success(true);
    }

    private Bed findCurrentBed(TransferRequest transferRequest) {
        if (transferRequest.getCurrentBedId() != null) {
            Bed bed = bedService.getById(transferRequest.getCurrentBedId());
            if (bed != null && transferRequest.getStudentId().equals(bed.getStudentId())) {
                return bed;
            }
        }

        QueryWrapper<Bed> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_id", transferRequest.getStudentId());
        List<Bed> beds = bedService.list(queryWrapper);
        return beds.isEmpty() ? null : beds.get(0);
    }

    private Bed findAvailableTargetBed(Long targetRoomId) {
        QueryWrapper<Bed> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("room_id", targetRoomId);
        return bedService.list(queryWrapper).stream()
            .filter(bed -> bed.getStudentId() == null)
            .filter(bed -> bed.getStatus() == null || "EMPTY".equals(bed.getStatus()))
            .findFirst()
            .orElse(null);
    }

    private void releaseBed(Bed bed) {
        UpdateWrapper<Bed> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", bed.getId())
            .set("student_id", null)
            .set("status", "EMPTY");
        bedService.update(updateWrapper);
        bed.setStudentId(null);
        bed.setStatus("EMPTY");
    }

    private void refreshRoomStatus(Long roomId) {
        if (roomId == null) {
            return;
        }

        Room room = roomService.getById(roomId);
        if (room == null || room.getCapacity() == null) {
            return;
        }

        QueryWrapper<Bed> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("room_id", roomId);
        long occupied = bedService.list(queryWrapper).stream()
            .filter(bed -> bed.getStudentId() != null || "OCCUPIED".equals(bed.getStatus()))
            .count();
        room.setStatus(occupied >= room.getCapacity() ? "FULL" : "NORMAL");
        roomService.updateById(room);
    }
}
