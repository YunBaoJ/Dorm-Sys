package com.dorm.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("bed")
public class Bed {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long roomId;
    private String bedNumber;
    private String status; // EMPTY, OCCUPIED, BROKEN
    private Long studentId;
    
    // Virtual fields
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String studentName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
