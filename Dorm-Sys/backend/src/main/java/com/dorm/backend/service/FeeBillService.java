package com.dorm.backend.service;

import com.dorm.backend.entity.FeeBill;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface FeeBillService extends IService<FeeBill> {
    List<FeeBill> listFeeBillsWithDetails(Long roomId, String status, String role, Long userId);
    FeeBill getFeeBillWithCheck(Long id, String role, Long userId);
}
