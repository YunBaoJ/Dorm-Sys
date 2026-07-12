package com.dorm.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("business_record")
public class BusinessRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String type;
    private String title;
    private String owner;
    private String description;
    private String status;
    private Long creatorId;
    private String reply;
    private LocalDateTime eventTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
