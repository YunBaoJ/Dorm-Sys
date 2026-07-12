package com.dorm.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("stay_history")
public class StayHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long bedId;
    private Date checkInDate;
    private Date checkOutDate;
    private Date createTime;
    private Date updateTime;
}