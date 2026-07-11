package com.dorm.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.backend.entity.BusinessRecord;
import com.dorm.backend.mapper.BusinessRecordMapper;
import com.dorm.backend.service.BusinessRecordService;
import org.springframework.stereotype.Service;

@Service
public class BusinessRecordServiceImpl extends ServiceImpl<BusinessRecordMapper, BusinessRecord> implements BusinessRecordService {
}
