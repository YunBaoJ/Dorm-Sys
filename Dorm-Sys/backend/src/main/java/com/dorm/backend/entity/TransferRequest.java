package com.dorm.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("transfer_request")
public class TransferRequest {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long currentBedId;
    private Long targetRoomId;
    private String reason;
    private String status; // PENDING, APPROVED, REJECTED
    
    // Virtual fields
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String studentName;
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String currentBedName;
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String targetRoomName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
