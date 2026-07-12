package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.entity.CallRecord;
import com.dorm.backend.service.CallRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/callRecord")
public class CallRecordController {

    @Autowired
    private CallRecordService callRecordService;

    @GetMapping("/list")
    public Result list(@RequestParam(required = false) Long studentId) {
        QueryWrapper<CallRecord> queryWrapper = new QueryWrapper<>();
        if (studentId != null) {
            queryWrapper.eq("student_id", studentId);
        }
        queryWrapper.orderByDesc("create_time");
        List<CallRecord> list = callRecordService.list(queryWrapper);
        return Result.success(list);
    }

    @PostMapping("/add")
    public Result add(@RequestBody CallRecord callRecord) {
        callRecord.setStatus("PENDING");
        boolean save = callRecordService.save(callRecord);
        if (save) {
            return Result.success();
        }
        return Result.error("添加失败");
    }
}
