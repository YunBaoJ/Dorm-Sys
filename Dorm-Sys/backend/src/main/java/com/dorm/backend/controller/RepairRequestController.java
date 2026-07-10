package com.dorm.backend.controller;

import com.dorm.backend.entity.RepairRequest;
import com.dorm.backend.service.RepairRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/repair-request")
public class RepairRequestController {

    @Autowired
    private RepairRequestService repairRequestService;

    @GetMapping("/list")
    public List<RepairRequest> list() {
        return repairRequestService.list();
    }

    @GetMapping("/{id}")
    public RepairRequest getById(@PathVariable Long id) {
        return repairRequestService.getById(id);
    }

    @PostMapping("/save")
    public boolean save(@RequestBody RepairRequest repairRequest) {
        return repairRequestService.saveOrUpdate(repairRequest);
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return repairRequestService.removeById(id);
    }
}
