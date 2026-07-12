package com.dorm.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.backend.entity.PatrolRecord;
import com.dorm.backend.mapper.PatrolRecordMapper;
import com.dorm.backend.service.PatrolRecordService;
import org.springframework.stereotype.Service;

@Service
public class PatrolRecordServiceImpl extends ServiceImpl<PatrolRecordMapper, PatrolRecord> implements PatrolRecordService {
}
