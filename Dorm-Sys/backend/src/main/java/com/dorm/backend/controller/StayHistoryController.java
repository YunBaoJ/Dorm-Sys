package com.dorm.backend.controller;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dorm.backend.entity.StayHistory;
import com.dorm.backend.service.StayHistoryService;
import com.dorm.backend.common.Result;
import com.dorm.backend.common.AuthUtils;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;

@RestController
@RequestMapping("/api/stayHistory")
public class StayHistoryController {
    private final StayHistoryService stayHistoryService;

    public StayHistoryController(StayHistoryService stayHistoryService) {
        this.stayHistoryService = stayHistoryService;
    }

    @GetMapping("/list")
    public Result<List<StayHistory>> list(@RequestParam(defaultValue = "1") Integer page,
                                          @RequestParam(defaultValue = "100") Integer size) {
        Long userId = AuthUtils.getCurrentUserId();
        QueryWrapper<StayHistory> query = new QueryWrapper<>();
        if ("student".equals(AuthUtils.getCurrentUserRole())) query.eq("student_id", userId);
        Page<StayHistory> pageResult = stayHistoryService.page(new Page<>(page, size), query);
        return Result.success(pageResult.getRecords());
    }

    @GetMapping("/current")
    public Result<StayHistory> current() {
        Long userId = AuthUtils.getCurrentUserId();
        if (userId == null) return Result.error(401, "未登录");
        QueryWrapper<StayHistory> query = new QueryWrapper<>();
        query.eq("student_id", userId)
             .isNull("check_out_date")
             .orderByDesc("check_in_date")
             .last("LIMIT 1");
        return Result.success(stayHistoryService.getOne(query));
    }

}
