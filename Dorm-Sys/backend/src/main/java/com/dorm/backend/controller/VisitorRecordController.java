package com.dorm.backend.controller;

import com.dorm.backend.entity.VisitorRecord;
import com.dorm.backend.service.VisitorRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/visitor-record")
public class VisitorRecordController {

    @Autowired
    private VisitorRecordService visitorRecordService;

    @GetMapping("/list")
    public List<VisitorRecord> list() {
        return visitorRecordService.list();
    }

    @GetMapping("/{id}")
    public VisitorRecord getById(@PathVariable Long id) {
        return visitorRecordService.getById(id);
    }

    @PostMapping("/save")
    public boolean save(@RequestBody VisitorRecord visitorRecord) {
        return visitorRecordService.saveOrUpdate(visitorRecord);
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return visitorRecordService.removeById(id);
    }
}
