package com.dorm.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.common.AuthUtils;
import com.dorm.backend.entity.ItemRecord;
import com.dorm.backend.service.ItemRecordService;
import com.dorm.backend.service.DormManagerScopeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/itemRecord")
public class ItemRecordController {

    private final ItemRecordService itemRecordService;
    private final DormManagerScopeService managerScopeService;

    public ItemRecordController(ItemRecordService itemRecordService, DormManagerScopeService managerScopeService) {
        this.itemRecordService = itemRecordService;
        this.managerScopeService = managerScopeService;
    }

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "100") Integer size) {
        QueryWrapper<ItemRecord> queryWrapper = new QueryWrapper<>();
        if (AuthUtils.isStudent()) {
            queryWrapper.eq("creator_id", AuthUtils.getCurrentUserId());
        } else if ("dormmanager".equals(AuthUtils.getCurrentUserRole())) {
            List<Long> studentIds = managerScopeService.managedStudentIds(AuthUtils.getCurrentUserId());
            if (studentIds.isEmpty()) return Result.success(List.of());
            queryWrapper.in("creator_id", studentIds);
        }
        queryWrapper.orderByDesc("create_time");
        Page<ItemRecord> pageResult = itemRecordService.page(new Page<>(page, size), queryWrapper);
        return Result.success(pageResult.getRecords());
    }

    @PostMapping("/save")
    public Result save(@RequestBody ItemRecord entity) {
        if (entity.getId() != null) {
            boolean update = itemRecordService.updateById(entity);
            return update ? Result.success() : Result.error("更新失败");
        }
        boolean save = itemRecordService.save(entity);
        return save ? Result.success() : Result.error("添加失败");
    }
    
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        return Result.success(itemRecordService.removeById(id));
    }
}
