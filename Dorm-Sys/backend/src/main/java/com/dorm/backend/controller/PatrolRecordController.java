package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.entity.PatrolRecord;
import com.dorm.backend.service.PatrolRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.dorm.backend.common.AuthUtils;
import com.dorm.backend.service.DormManagerScopeService;

@RestController
@RequestMapping("/api/patrolRecord")
public class PatrolRecordController {

    @Autowired
    private PatrolRecordService patrolRecordService;

    @Autowired
    private DormManagerScopeService managerScopeService;

    @GetMapping("/list")
    public Result list() {
        QueryWrapper<PatrolRecord> queryWrapper = new QueryWrapper<>();
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole())) {
            List<String> buildingNames = managerScopeService.managedBuildingNames(AuthUtils.getCurrentUserId());
            if (buildingNames.isEmpty()) return Result.success(List.of());
            queryWrapper.in("building_name", buildingNames);
        }
        queryWrapper.orderByDesc("create_time");
        return Result.success(patrolRecordService.list(queryWrapper));
    }

    @PostMapping("/add")
    public Result add(@RequestBody PatrolRecord entity) {
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole())
                && !managerScopeService.managedBuildingNames(AuthUtils.getCurrentUserId()).contains(entity.getBuildingName())) {
            return Result.error(403, "无权新增该楼栋巡查记录");
        }
        boolean save = patrolRecordService.save(entity);
        if (save) {
            return Result.success();
        }
        return Result.error("添加失败");
    }

    @PostMapping("/update")
    public Result update(@RequestBody PatrolRecord entity) {
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole())) {
            PatrolRecord existing = entity.getId() == null ? null : patrolRecordService.getById(entity.getId());
            if (existing == null || !managerScopeService.managedBuildingNames(AuthUtils.getCurrentUserId())
                    .contains(existing.getBuildingName())) {
                return Result.error(403, "无权修改该巡查记录");
            }
        }
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole())
                && !managerScopeService.managedBuildingNames(AuthUtils.getCurrentUserId()).contains(entity.getBuildingName())) {
            return Result.error(403, "无权修改该楼栋巡查记录");
        }
        boolean update = patrolRecordService.updateById(entity);
        if (update) {
            return Result.success();
        }
        return Result.error("更新失败");
    }
    
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        PatrolRecord record = patrolRecordService.getById(id);
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole()) && record != null
                && !managerScopeService.managedBuildingNames(AuthUtils.getCurrentUserId()).contains(record.getBuildingName())) {
            return Result.error(403, "无权删除该楼栋巡查记录");
        }
        return Result.success(patrolRecordService.removeById(id));
    }
}
