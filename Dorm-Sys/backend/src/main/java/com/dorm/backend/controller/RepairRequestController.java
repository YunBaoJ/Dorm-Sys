package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.entity.RepairRequest;
import com.dorm.backend.entity.User;
import com.dorm.backend.entity.Room;
import com.dorm.backend.entity.Building;
import com.dorm.backend.service.RepairRequestService;
import com.dorm.backend.service.UserService;
import com.dorm.backend.service.RoomService;
import com.dorm.backend.service.BuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/repairRequest")
public class RepairRequestController {

    @Autowired
    private RepairRequestService repairRequestService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private RoomService roomService;
    
    @Autowired
    private BuildingService buildingService;

    @GetMapping("/list")
    public Result<List<RepairRequest>> list(@RequestParam(required = false) Long submitterId, 
                                            @RequestParam(required = false) String status) {
        QueryWrapper<RepairRequest> queryWrapper = new QueryWrapper<>();
        if (submitterId != null) queryWrapper.eq("submitter_id", submitterId);
        if (status != null && !status.isEmpty()) queryWrapper.eq("status", status);
        
        queryWrapper.orderByDesc("create_time");
        List<RepairRequest> list = repairRequestService.list(queryWrapper);
        
        Map<Long, String> userMap = userService.list().stream()
            .collect(Collectors.toMap(User::getId, User::getName));
            
        Map<Long, Room> roomMap = roomService.list().stream()
            .collect(Collectors.toMap(Room::getId, r -> r));
            
        Map<Long, String> buildingMap = buildingService.list().stream()
            .collect(Collectors.toMap(Building::getId, Building::getName));
            
        for (RepairRequest req : list) {
            req.setSubmitterName(userMap.get(req.getSubmitterId()));
            if (req.getHandlerId() != null) req.setHandlerName(userMap.get(req.getHandlerId()));
            
            Room r = roomMap.get(req.getRoomId());
            if (r != null) {
                String bName = buildingMap.get(r.getBuildingId());
                req.setRoomName((bName != null ? bName : "") + " " + r.getRoomNumber());
            }
        }
        
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<RepairRequest> getById(@PathVariable Long id) {
        return Result.success(repairRequestService.getById(id));
    }

    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody RepairRequest repairRequest) {
        return Result.success(repairRequestService.saveOrUpdate(repairRequest));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(repairRequestService.removeById(id));
    }
}
