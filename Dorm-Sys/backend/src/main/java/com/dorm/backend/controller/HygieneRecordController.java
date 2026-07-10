package com.dorm.backend.controller;

import com.dorm.backend.entity.HygieneRecord;
import com.dorm.backend.service.HygieneRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/hygiene-record")
public class HygieneRecordController {

    @Autowired
    private HygieneRecordService hygieneRecordService;

    @GetMapping("/list")
    public List<HygieneRecord> list() {
        return hygieneRecordService.list();
    }

    @GetMapping("/{id}")
    public HygieneRecord getById(@PathVariable Long id) {
        return hygieneRecordService.getById(id);
    }

    @PostMapping("/save")
    public boolean save(@RequestBody HygieneRecord hygieneRecord) {
        return hygieneRecordService.saveOrUpdate(hygieneRecord);
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return hygieneRecordService.removeById(id);
    }
}
