package com.dorm.backend.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("admin_info")
public class AdminInfo {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String department;
}