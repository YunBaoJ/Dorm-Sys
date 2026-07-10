package com.dorm.backend.service.impl;

import com.dorm.backend.entity.RepairRequest;
import com.dorm.backend.mapper.RepairRequestMapper;
import com.dorm.backend.service.RepairRequestService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class RepairRequestServiceImpl extends ServiceImpl<RepairRequestMapper, RepairRequest> implements RepairRequestService {
}
