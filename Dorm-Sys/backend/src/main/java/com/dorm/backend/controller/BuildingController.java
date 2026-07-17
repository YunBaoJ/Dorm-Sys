package com.dorm.backend.controller;

import com.dorm.backend.common.Result;
import com.dorm.backend.entity.Building;
import com.dorm.backend.entity.Room;
import com.dorm.backend.entity.Bed;
import com.dorm.backend.service.BuildingService;
import com.dorm.backend.service.RoomService;
import com.dorm.backend.service.BedService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.dorm.backend.common.AuthUtils;
import com.dorm.backend.entity.User;
import com.dorm.backend.service.UserService;

@RestController
@RequestMapping("/api/building")
public class BuildingController {

    @Autowired
    private BuildingService buildingService;
    
    @Autowired
    private RoomService roomService;
    
    @Autowired
    private BedService bedService;
    
    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Result<List<Building>> list() {
        QueryWrapper<Building> bQw = new QueryWrapper<>();
        
        String role = AuthUtils.getCurrentUserRole();
        Long userId = AuthUtils.getCurrentUserId();
        
        if ("dormmanager".equals(role) && userId != null) {
            User user = userService.getById(userId);
            if (user != null && user.getName() != null) {
                bQw.eq("manager", user.getName());
            }
        }
        
        List<Building> buildings = buildingService.getBuildingsWithStats(bQw);

        return Result.success(buildings);
    }

    @GetMapping("/{id}")
    public Result<Building> getById(@PathVariable Long id) {
        return Result.success(buildingService.getById(id));
    }

    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody Building building) {
        return Result.success(buildingService.saveOrUpdate(building));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(buildingService.removeById(id));
    }
}
