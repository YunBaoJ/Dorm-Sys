package com.dorm.backend.service.impl;

import com.dorm.backend.entity.TransferRequest;
import com.dorm.backend.mapper.TransferRequestMapper;
import com.dorm.backend.service.TransferRequestService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class TransferRequestServiceImpl extends ServiceImpl<TransferRequestMapper, TransferRequest> implements TransferRequestService {
}
