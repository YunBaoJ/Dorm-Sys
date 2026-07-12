package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.entity.Feedback;
import com.dorm.backend.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @GetMapping("/list")
    public Result list(@RequestParam(required = false) Long studentId) {
        QueryWrapper<Feedback> queryWrapper = new QueryWrapper<>();
        if (studentId != null) {
            queryWrapper.eq("student_id", studentId);
        }
        queryWrapper.orderByDesc("create_time");
        List<Feedback> list = feedbackService.list(queryWrapper);
        return Result.success(list);
    }

    @PostMapping("/add")
    public Result add(@RequestBody Feedback feedback) {
        feedback.setStatus("UNREAD");
        boolean save = feedbackService.save(feedback);
        if (save) {
            return Result.success();
        }
        return Result.error("添加失败");
    }

    @PostMapping("/reply")
    public Result reply(@RequestBody Feedback feedback) {
        if (feedback.getId() == null) {
            return Result.error("ID不能为空");
        }
        feedback.setStatus("REPLIED");
        boolean update = feedbackService.updateById(feedback);
        if (update) {
            return Result.success();
        }
        return Result.error("回复失败");
    }
}
