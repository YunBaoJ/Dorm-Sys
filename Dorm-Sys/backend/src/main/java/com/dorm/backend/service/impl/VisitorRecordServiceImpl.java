package com.dorm.backend.service.impl;

import com.dorm.backend.entity.VisitorRecord;
import com.dorm.backend.mapper.VisitorRecordMapper;
import com.dorm.backend.service.VisitorRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class VisitorRecordServiceImpl extends ServiceImpl<VisitorRecordMapper, VisitorRecord> implements VisitorRecordService {
}
