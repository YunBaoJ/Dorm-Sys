package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.entity.Bed;
import com.dorm.backend.entity.User;
import com.dorm.backend.entity.StayHistory;
import com.dorm.backend.service.BedService;
import com.dorm.backend.service.UserService;
import com.dorm.backend.service.StayHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.Objects;

@RestController
@RequestMapping("/api/bed")
public class BedController {

    @Autowired
    private BedService bedService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private StayHistoryService stayHistoryService;

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
        if (bed.getId() != null) {
            Bed existingBed = bedService.getById(bed.getId());
            if (existingBed != null) {
                Long oldStudentId = existingBed.getStudentId();
                Long newStudentId = bed.getStudentId();
                
                if (!Objects.equals(oldStudentId, newStudentId)) {
                    // Someone is moving out
                    if (oldStudentId != null) {
                        QueryWrapper<StayHistory> query = new QueryWrapper<>();
                        query.eq("student_id", oldStudentId)
                             .eq("bed_id", bed.getId())
                             .isNull("check_out_date")
                             .orderByDesc("check_in_date")
                             .last("LIMIT 1");
                        StayHistory history = stayHistoryService.getOne(query);
                        if (history != null) {
                            history.setCheckOutDate(new Date());
                            stayHistoryService.updateById(history);
                        }
                    }
                    // Someone is moving in
                    if (newStudentId != null) {
                        StayHistory newHistory = new StayHistory();
                        newHistory.setStudentId(newStudentId);
                        newHistory.setBedId(bed.getId());
                        newHistory.setCheckInDate(new Date());
                        stayHistoryService.save(newHistory);
                    }
                }
            }
        }
        return Result.success(bedService.saveOrUpdate(bed));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(bedService.removeById(id));
    }
}
