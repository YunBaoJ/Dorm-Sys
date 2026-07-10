package com.dorm.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("hygiene_record")
public class HygieneRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
