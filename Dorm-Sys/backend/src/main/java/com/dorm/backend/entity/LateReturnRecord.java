package com.dorm.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("late_return_record")
public class LateReturnRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private String studentName;
    private String roomNumber;
    private String reason;
    private String status;
    private java.time.LocalDateTime returnTime;
    private java.time.LocalDateTime createTime;
    private java.time.LocalDateTime updateTime;
}
