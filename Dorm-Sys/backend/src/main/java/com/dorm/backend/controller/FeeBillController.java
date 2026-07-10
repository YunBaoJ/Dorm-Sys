package com.dorm.backend.controller;

import com.dorm.backend.entity.FeeBill;
import com.dorm.backend.service.FeeBillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/fee-bill")
public class FeeBillController {

    @Autowired
    private FeeBillService feeBillService;

    @GetMapping("/list")
    public List<FeeBill> list() {
        return feeBillService.list();
    }

    @GetMapping("/{id}")
    public FeeBill getById(@PathVariable Long id) {
        return feeBillService.getById(id);
    }

    @PostMapping("/save")
    public boolean save(@RequestBody FeeBill feeBill) {
        return feeBillService.saveOrUpdate(feeBill);
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return feeBillService.removeById(id);
    }
}
