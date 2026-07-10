package com.dorm.backend.service.impl;

import com.dorm.backend.entity.Building;
import com.dorm.backend.mapper.BuildingMapper;
import com.dorm.backend.service.BuildingService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class BuildingServiceImpl extends ServiceImpl<BuildingMapper, Building> implements BuildingService {
}
