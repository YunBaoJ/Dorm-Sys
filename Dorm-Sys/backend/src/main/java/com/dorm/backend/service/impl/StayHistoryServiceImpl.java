package com.dorm.backend.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.backend.entity.StayHistory;
import com.dorm.backend.mapper.StayHistoryMapper;
import com.dorm.backend.service.StayHistoryService;
import org.springframework.stereotype.Service;

@Service
public class StayHistoryServiceImpl extends ServiceImpl<StayHistoryMapper, StayHistory> implements StayHistoryService {
}