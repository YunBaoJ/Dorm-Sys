package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.entity.BusinessRecord;
import com.dorm.backend.service.BusinessRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/businessRecord")
public class BusinessRecordController {

    @Autowired
    private BusinessRecordService businessRecordService;

    @GetMapping("/list")
    public Result<List<BusinessRecord>> list(@RequestParam(required = false) String type,
                                             @RequestParam(required = false) String status) {
        QueryWrapper<BusinessRecord> queryWrapper = new QueryWrapper<>();
        if (type != null && !type.isBlank()) {
            queryWrapper.eq("type", type);
        }
        if (status != null && !status.isBlank()) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.orderByDesc("create_time");
        return Result.success(businessRecordService.list(queryWrapper));
    }

    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody BusinessRecord record) {
        if (record.getType() == null || record.getType().isBlank()) {
            return Result.error(400, "业务类型不能为空");
        }
        if (record.getTitle() == null || record.getTitle().isBlank()) {
            return Result.error(400, "标题不能为空");
        }

        LocalDateTime now = LocalDateTime.now();
        if (record.getId() == null && record.getCreateTime() == null) {
            record.setCreateTime(now);
        }
        record.setUpdateTime(now);
        return Result.success(businessRecordService.saveOrUpdate(record));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(businessRecordService.removeById(id));
    }
}
