package com.dorm.backend.service;

import com.dorm.backend.entity.HygieneRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface HygieneRecordService extends IService<HygieneRecord> {
    List<HygieneRecord> listHygieneRecordsWithDetails(Long roomId, String role, Long userId);
}
