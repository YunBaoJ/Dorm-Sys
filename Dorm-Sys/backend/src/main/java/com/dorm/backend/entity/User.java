package com.dorm.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
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
    private String gender;
    private String avatar;
    private String email;
    private String phone;

    @TableField(exist = false)
    private String className;
    @TableField(exist = false)
    private String major;
    private Boolean enabled;
    
    private java.time.LocalDateTime createTime;
    private java.time.LocalDateTime updateTime;
}
