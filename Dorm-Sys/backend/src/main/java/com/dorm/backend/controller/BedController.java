package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.entity.Bed;
import com.dorm.backend.entity.User;
import com.dorm.backend.service.BedService;
import com.dorm.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bed")
public class BedController {

    @Autowired
    private BedService bedService;
    
    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Result<List<Bed>> list(@RequestParam(required = false) Long roomId) {
        QueryWrapper<Bed> queryWrapper = new QueryWrapper<>();
        if (roomId != null) {
            queryWrapper.eq("room_id", roomId);
        }
        List<Bed> beds = bedService.list(queryWrapper);
        
        // Populate student names
        List<User> students = userService.list();
        Map<Long, String> studentMap = students.stream()
            .collect(Collectors.toMap(User::getId, User::getName));
            
        for (Bed bed : beds) {
            if (bed.getStudentId() != null) {
                bed.setStudentName(studentMap.get(bed.getStudentId()));
            }
        }
        
        return Result.success(beds);
    }

    @GetMapping("/{id}")
    public Result<Bed> getById(@PathVariable Long id) {
        return Result.success(bedService.getById(id));
    }

    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody Bed bed) {
        return Result.success(bedService.saveOrUpdate(bed));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(bedService.removeById(id));
    }
}
