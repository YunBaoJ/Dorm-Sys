package com.dorm.backend.service;

import com.dorm.backend.entity.RepairRequest;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface RepairRequestService extends IService<RepairRequest> {
    List<RepairRequest> listRepairRequestsWithDetails(Long submitterId, String status, String role, Long userId);
}
