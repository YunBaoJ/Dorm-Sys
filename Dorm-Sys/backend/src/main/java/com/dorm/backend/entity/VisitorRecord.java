package com.dorm.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("visitor_record")
public class VisitorRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
