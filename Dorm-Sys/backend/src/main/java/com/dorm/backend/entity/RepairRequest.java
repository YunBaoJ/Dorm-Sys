package com.dorm.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("repair_request")
public class RepairRequest {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long submitterId;
    private Long roomId;
    private String type;
    private String description;
    private String images;
    private String status; // PENDING, PROCESSING, COMPLETED
    private Long handlerId;
    
    // Virtual fields
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String submitterName;
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String roomName;
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String handlerName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
