package com.dorm.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.backend.entity.ItemRecord;
import com.dorm.backend.mapper.ItemRecordMapper;
import com.dorm.backend.service.ItemRecordService;
import org.springframework.stereotype.Service;

@Service
public class ItemRecordServiceImpl extends ServiceImpl<ItemRecordMapper, ItemRecord> implements ItemRecordService {
}
