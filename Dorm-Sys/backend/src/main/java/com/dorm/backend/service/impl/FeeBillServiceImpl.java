package com.dorm.backend.service.impl;

import com.dorm.backend.entity.FeeBill;
import com.dorm.backend.mapper.FeeBillMapper;
import com.dorm.backend.service.FeeBillService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class FeeBillServiceImpl extends ServiceImpl<FeeBillMapper, FeeBill> implements FeeBillService {
}
