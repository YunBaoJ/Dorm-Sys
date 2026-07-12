package com.dorm.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("call_record")
public class CallRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private String topic;
    private String targetPerson;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
