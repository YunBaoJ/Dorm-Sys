package com.dorm.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.backend.entity.CallRecord;
import com.dorm.backend.mapper.CallRecordMapper;
import com.dorm.backend.service.CallRecordService;
import org.springframework.stereotype.Service;

@Service
public class CallRecordServiceImpl extends ServiceImpl<CallRecordMapper, CallRecord> implements CallRecordService {
}
