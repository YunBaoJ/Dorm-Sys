package com.dorm.backend.service.impl;

import com.dorm.backend.entity.Bed;
import com.dorm.backend.mapper.BedMapper;
import com.dorm.backend.service.BedService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class BedServiceImpl extends ServiceImpl<BedMapper, Bed> implements BedService {
}
