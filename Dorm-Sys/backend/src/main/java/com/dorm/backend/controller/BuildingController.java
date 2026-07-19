package com.dorm.backend.controller;

import com.dorm.backend.common.Result;
import com.dorm.backend.entity.Building;
import com.dorm.backend.service.BuildingService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.dorm.backend.common.AuthUtils;
import com.dorm.backend.service.DormManagerScopeService;

@RestController
@RequestMapping("/api/building")
public class BuildingController {

    @Autowired
    private BuildingService buildingService;
    
    @Autowired
    private DormManagerScopeService managerScopeService;

    @GetMapping("/list")
    public Result<List<Building>> list() {
        QueryWrapper<Building> bQw = new QueryWrapper<>();
        
        String role = AuthUtils.getCurrentUserRole();
        Long userId = AuthUtils.getCurrentUserId();
        
        if ("dormmanager".equals(role) && userId != null) {
            List<Long> buildingIds = managerScopeService.managedBuildingIds(userId);
            if (buildingIds.isEmpty()) return Result.success(List.of());
            bQw.in("id", buildingIds);
        }
        
        List<Building> buildings = buildingService.getBuildingsWithStats(bQw);

        return Result.success(buildings);
    }

    @GetMapping("/{id}")
    public Result<Building> getById(@PathVariable Long id) {
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole())
                && !managerScopeService.canManageBuilding(AuthUtils.getCurrentUserId(), id)) {
            return Result.error(403, "无权查看该楼栋");
        }
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
