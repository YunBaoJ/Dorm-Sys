package com.dorm.backend.controller;
import com.dorm.backend.entity.StayHistory;
import com.dorm.backend.service.StayHistoryService;
import com.dorm.backend.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/stayHistory")
public class StayHistoryController {
    @Autowired
    private StayHistoryService stayHistoryService;

    @GetMapping("/list")
    public Result<List<StayHistory>> list() {
        return Result.success(stayHistoryService.list());
    }
}