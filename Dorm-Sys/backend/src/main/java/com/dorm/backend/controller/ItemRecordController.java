package com.dorm.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.common.AuthUtils;
import com.dorm.backend.entity.ItemRecord;
import com.dorm.backend.service.ItemRecordService;
import com.dorm.backend.service.DormManagerScopeService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    public Result<List<ItemRecord>> list(@RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "100") Integer size) {
        QueryWrapper<ItemRecord> queryWrapper = new QueryWrapper<>();
        Long userId = AuthUtils.getCurrentUserId();
        String role = AuthUtils.getCurrentUserRole();
        if ("student".equals(role)) {
            return Result.error(403, "学生无权查看物品出入记录");
        } else if ("dormmanager".equals(role)) {
            List<Long> studentIds = managerScopeService.managedStudentIds(userId);
            queryWrapper.and(w -> {
                w.eq("creator_id", userId);
                if (!studentIds.isEmpty()) {
                    w.or().in("creator_id", studentIds);
                }
            });
        }
        queryWrapper.orderByDesc("create_time");
        Page<ItemRecord> pageResult = itemRecordService.page(new Page<>(page, size), queryWrapper);
        return Result.success(pageResult.getRecords());
    }

    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody ItemRecord entity) {
        String role = AuthUtils.getCurrentUserRole();
        if ("student".equals(role)) {
            return Result.error(403, "学生无权操作物品出入记录");
        }
        Long userId = AuthUtils.getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        if (entity.getId() != null) {
            // Update - check ownership
            ItemRecord existing = itemRecordService.getById(entity.getId());
            if (existing == null) {
                return Result.error(404, "记录不存在");
            }
            if ("dormmanager".equals(role) && !userId.equals(existing.getCreatorId())) {
                return Result.error(403, "无权修改他人创建的记录");
            }
            entity.setCreatorId(existing.getCreatorId());
            entity.setCreateTime(existing.getCreateTime());
        } else {
            entity.setCreatorId(userId);
            entity.setCreateTime(now);
        }
        entity.setUpdateTime(now);
        return Result.success(itemRecordService.saveOrUpdate(entity));
    }
    
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        String role = AuthUtils.getCurrentUserRole();
        if ("student".equals(role)) {
            return Result.error(403, "学生无权删除物品出入记录");
        }
        ItemRecord existing = itemRecordService.getById(id);
        if (existing == null) {
            return Result.error(404, "记录不存在");
        }
        if ("dormmanager".equals(role) && !AuthUtils.getCurrentUserId().equals(existing.getCreatorId())) {
            return Result.error(403, "无权删除他人创建的记录");
        }
        return Result.success(itemRecordService.removeById(id));
    }
}
