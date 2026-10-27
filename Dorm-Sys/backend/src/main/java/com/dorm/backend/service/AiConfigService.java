package com.dorm.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dorm.backend.entity.AiConfig;

public interface AiConfigService extends IService<AiConfig> {
    AiConfig getOrCreateConfig();
}
