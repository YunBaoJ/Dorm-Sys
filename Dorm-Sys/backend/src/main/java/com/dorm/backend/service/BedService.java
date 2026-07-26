package com.dorm.backend.service;

import com.dorm.backend.entity.Bed;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface BedService extends IService<Bed> {
    List<Bed> listBedsWithDetails(Long roomId, String status, String role, Long userId);
}
