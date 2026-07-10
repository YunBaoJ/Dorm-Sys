package com.dorm.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String role; // student, dormmanager, admin
    private String name;
    private String avatar;
    private String className;
    private String email;
    private String phone;
    private Boolean enabled;
    
    private java.time.LocalDateTime createTime;
    private java.time.LocalDateTime updateTime;
}
