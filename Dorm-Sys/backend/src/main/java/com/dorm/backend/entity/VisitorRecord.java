package com.dorm.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("visitor_record")
public class VisitorRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private String visitorName;
    private String phone;
    private String relation;
    private LocalDateTime visitTime;
    private LocalDateTime leaveTime;
    private String status; // PENDING, APPROVED, LEFT
    
    @TableField(exist = false)
    private String studentName;
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
