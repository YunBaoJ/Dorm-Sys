package com.dorm.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("building")
public class Building {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    private String type; // 男生楼/女生楼
    private Integer floors;
    private String manager;
    private String location;
    private Boolean active;
    
    // 聚合统计字段（可选，在数据库可以有或者没有）
    private Integer totalRooms;
    private Integer occupiedRooms;
    private Integer freeRooms;
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
