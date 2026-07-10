package com.dorm.backend.controller;

import com.dorm.backend.entity.Building;
import com.dorm.backend.service.BuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/building")
public class BuildingController {

    @Autowired
    private BuildingService buildingService;

    @GetMapping("/list")
    public List<Building> list() {
        return buildingService.list();
    }

    @GetMapping("/{id}")
    public Building getById(@PathVariable Long id) {
        return buildingService.getById(id);
    }

    @PostMapping("/save")
    public boolean save(@RequestBody Building building) {
        return buildingService.saveOrUpdate(building);
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return buildingService.removeById(id);
    }
}
