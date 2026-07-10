package com.dorm.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("room")
public class Room {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long buildingId;
    private String roomNumber;
    private Integer floor;
    private Integer capacity;
    private String status; // NORMAL, FULL, MAINTENANCE
    
    // Virtual fields
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String buildingName;
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private Integer occupied;
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
