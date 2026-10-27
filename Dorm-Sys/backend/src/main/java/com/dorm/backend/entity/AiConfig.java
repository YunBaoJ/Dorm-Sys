package com.dorm.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_config")
public class AiConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String botName;
    private String personaType; // vibrant, gentle, humorous, formal, custom
    private String customPrompt;
    private Double temperature;
    private String model;
    private String apiKey;
    private LocalDateTime updateTime;
}
