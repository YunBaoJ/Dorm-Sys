package com.dorm.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("hygiene_record")
public class HygieneRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long roomId;
    private Long inspectorId;
    private Integer score;
    private String comment;
    private LocalDate checkDate;
    
    @TableField(exist = false)
    private String roomName;
    @TableField(exist = false)
    private String inspectorName;
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
