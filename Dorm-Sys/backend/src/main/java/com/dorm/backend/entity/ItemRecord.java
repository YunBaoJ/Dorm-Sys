package com.dorm.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("item_record")
public class ItemRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String owner;
    private String description;
    private String status;
    private java.time.LocalDateTime createTime;
    private java.time.LocalDateTime updateTime;
}
