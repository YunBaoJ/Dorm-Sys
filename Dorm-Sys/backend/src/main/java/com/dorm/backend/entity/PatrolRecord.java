package com.dorm.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("patrol_record")
public class PatrolRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String buildingName;
    private String area;
    private String issue;
    private String imageUrl;
    private String status;
    private java.time.LocalDateTime createTime;
    private java.time.LocalDateTime updateTime;
}
