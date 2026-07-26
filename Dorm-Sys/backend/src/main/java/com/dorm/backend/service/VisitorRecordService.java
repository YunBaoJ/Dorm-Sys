package com.dorm.backend.service;

import com.dorm.backend.entity.VisitorRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface VisitorRecordService extends IService<VisitorRecord> {
    List<VisitorRecord> listVisitorRecordsWithDetails(Long studentId, String role, Long userId);
}
