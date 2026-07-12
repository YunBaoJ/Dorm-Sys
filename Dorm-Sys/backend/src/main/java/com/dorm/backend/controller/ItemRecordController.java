package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.entity.ItemRecord;
import com.dorm.backend.service.ItemRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/itemRecord")
public class ItemRecordController {

    @Autowired
    private ItemRecordService itemRecordService;

    @GetMapping("/list")
    public Result list() {
        QueryWrapper<ItemRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        return Result.success(itemRecordService.list(queryWrapper));
    }

    @PostMapping("/add")
    public Result add(@RequestBody ItemRecord entity) {
        boolean save = itemRecordService.save(entity);
        if (save) {
            return Result.success();
        }
        return Result.error("添加失败");
    }

    @PostMapping("/update")
    public Result update(@RequestBody ItemRecord entity) {
        boolean update = itemRecordService.updateById(entity);
        if (update) {
            return Result.success();
        }
        return Result.error("更新失败");
    }
    
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        return Result.success(itemRecordService.removeById(id));
    }
}
