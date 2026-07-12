package com.dorm.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.backend.entity.LateReturnRecord;
import com.dorm.backend.mapper.LateReturnRecordMapper;
import com.dorm.backend.service.LateReturnRecordService;
import org.springframework.stereotype.Service;

@Service
public class LateReturnRecordServiceImpl extends ServiceImpl<LateReturnRecordMapper, LateReturnRecord> implements LateReturnRecordService {
}
