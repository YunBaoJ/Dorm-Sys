package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
    public Result<Boolean> save(@RequestBody TransferRequest transferRequest) {
        return Result.success(transferRequestService.saveOrUpdate(transferRequest));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(transferRequestService.removeById(id));
    }
}
