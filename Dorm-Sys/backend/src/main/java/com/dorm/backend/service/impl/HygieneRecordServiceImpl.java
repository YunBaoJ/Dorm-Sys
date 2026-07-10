package com.dorm.backend.service.impl;

import com.dorm.backend.entity.HygieneRecord;
import com.dorm.backend.mapper.HygieneRecordMapper;
import com.dorm.backend.service.HygieneRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class HygieneRecordServiceImpl extends ServiceImpl<HygieneRecordMapper, HygieneRecord> implements HygieneRecordService {
}
