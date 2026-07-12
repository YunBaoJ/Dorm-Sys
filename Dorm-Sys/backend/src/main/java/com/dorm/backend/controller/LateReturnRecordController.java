package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.entity.LateReturnRecord;
import com.dorm.backend.service.LateReturnRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lateReturnRecord")
public class LateReturnRecordController {

    @Autowired
    private LateReturnRecordService lateReturnRecordService;

    @GetMapping("/list")
    public Result list() {
        QueryWrapper<LateReturnRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        return Result.success(lateReturnRecordService.list(queryWrapper));
    }

    @PostMapping("/add")
    public Result add(@RequestBody LateReturnRecord entity) {
        boolean save = lateReturnRecordService.save(entity);
        if (save) {
            return Result.success();
        }
        return Result.error("添加失败");
    }

    @PostMapping("/update")
    public Result update(@RequestBody LateReturnRecord entity) {
        boolean update = lateReturnRecordService.updateById(entity);
        if (update) {
            return Result.success();
        }
        return Result.error("更新失败");
    }
    
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        return Result.success(lateReturnRecordService.removeById(id));
    }
}
