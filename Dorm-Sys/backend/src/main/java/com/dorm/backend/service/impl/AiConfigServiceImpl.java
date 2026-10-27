package com.dorm.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.backend.entity.AiConfig;
import com.dorm.backend.mapper.AiConfigMapper;
import com.dorm.backend.service.AiConfigService;
import org.springframework.stereotype.Service;

@Service
public class AiConfigServiceImpl extends ServiceImpl<AiConfigMapper, AiConfig> implements AiConfigService {

    @Override
    public AiConfig getOrCreateConfig() {
        AiConfig config = this.getById(1L);
        if (config == null) {
            config = new AiConfig();
            config.setId(1L);
            config.setBotName("小寝管家");
            config.setPersonaType("vibrant");
            config.setTemperature(0.85);
            config.setModel("Qwen/Qwen2.5-7B-Instruct");
            this.save(config);
        }
        return config;
    }
}
