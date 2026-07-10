package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.entity.VisitorRecord;
import com.dorm.backend.entity.User;
import com.dorm.backend.service.VisitorRecordService;
import com.dorm.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/visitorRecord")
public class VisitorRecordController {

    @Autowired
    private VisitorRecordService visitorRecordService;
    
    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Result<List<VisitorRecord>> list(@RequestParam(required = false) Long studentId) {
        QueryWrapper<VisitorRecord> qw = new QueryWrapper<>();
        if (studentId != null) qw.eq("student_id", studentId);
        qw.orderByDesc("create_time");
        
        List<VisitorRecord> list = visitorRecordService.list(qw);
        Map<Long, String> userMap = userService.list().stream()
            .collect(Collectors.toMap(User::getId, User::getName));
        for (VisitorRecord vr : list) {
            vr.setStudentName(userMap.get(vr.getStudentId()));
        }
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<VisitorRecord> getById(@PathVariable Long id) {
        return Result.success(visitorRecordService.getById(id));
    }

    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody VisitorRecord visitorRecord) {
        return Result.success(visitorRecordService.saveOrUpdate(visitorRecord));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(visitorRecordService.removeById(id));
    }
}
