package com.dorm.backend.service;

import com.dorm.backend.entity.Building;
import com.baomidou.mybatisplus.extension.service.IService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;

public interface BuildingService extends IService<Building> {
    List<Building> getBuildingsWithStats(QueryWrapper<Building> queryWrapper);
}
