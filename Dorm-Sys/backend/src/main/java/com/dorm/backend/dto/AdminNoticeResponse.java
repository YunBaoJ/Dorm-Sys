package com.dorm.backend.dto;

import com.dorm.backend.entity.BusinessRecord;

import java.time.LocalDateTime;

public record AdminNoticeResponse(
        Long id,
        String title,
        String owner,
        String description,
        String status,
        LocalDateTime eventTime,
        LocalDateTime createTime) {

    public static AdminNoticeResponse from(BusinessRecord record) {
        return new AdminNoticeResponse(
                record.getId(),
                record.getTitle(),
                record.getOwner(),
                record.getDescription(),
                record.getStatus(),
                record.getEventTime(),
                record.getCreateTime());
    }
}
