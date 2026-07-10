package com.dorm.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("fee_bill")
public class FeeBill {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long roomId;
    private String type; // WATER, ELECTRICITY
    private BigDecimal amount;
    private String month; // e.g. 2026-01
    private String status; // UNPAID, PAID
    
    @TableField(exist = false)
    private String roomName;
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
