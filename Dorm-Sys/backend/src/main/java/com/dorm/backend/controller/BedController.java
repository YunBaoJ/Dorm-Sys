package com.dorm.backend.controller;

import com.dorm.backend.entity.Bed;
import com.dorm.backend.service.BedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/bed")
public class BedController {

    @Autowired
    private BedService bedService;

    @GetMapping("/list")
    public List<Bed> list() {
        return bedService.list();
    }

    @GetMapping("/{id}")
    public Bed getById(@PathVariable Long id) {
        return bedService.getById(id);
    }

    @PostMapping("/save")
    public boolean save(@RequestBody Bed bed) {
        return bedService.saveOrUpdate(bed);
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return bedService.removeById(id);
    }
}
