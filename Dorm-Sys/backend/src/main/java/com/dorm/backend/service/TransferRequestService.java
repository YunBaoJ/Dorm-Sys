package com.dorm.backend.service;

import com.dorm.backend.entity.TransferRequest;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface TransferRequestService extends IService<TransferRequest> {
    List<TransferRequest> listTransferRequestsWithDetails(Long studentId, String status, String role, Long userId);
}
