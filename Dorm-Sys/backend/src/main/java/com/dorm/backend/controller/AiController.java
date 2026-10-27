package com.dorm.backend.controller;

import com.dorm.backend.common.AuthUtils;
import com.dorm.backend.common.Result;
import com.dorm.backend.dto.AiChatRequest;
import com.dorm.backend.entity.AiConfig;
import com.dorm.backend.service.AiConfigService;
import com.dorm.backend.service.AiService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;
    private final AiConfigService aiConfigService;

    public AiController(AiService aiService, AiConfigService aiConfigService) {
        this.aiService = aiService;
        this.aiConfigService = aiConfigService;
    }

    @PostMapping("/chat")
    public Result<String> chatPost(@RequestBody(required = false) AiChatRequest chatRequest) {
        Long currentUserId = AuthUtils.getCurrentUserId();
        String answer = aiService.chat(currentUserId, chatRequest);
        return Result.success(answer);
    }

    @GetMapping("/chat")
    public Result<String> chatGet(@RequestParam(required = false) String question) {
        Long currentUserId = AuthUtils.getCurrentUserId();
        AiChatRequest request = new AiChatRequest();
        request.setQuestion(question);
        String answer = aiService.chat(currentUserId, request);
        return Result.success(answer);
    }

    @GetMapping("/config")
    public Result<AiConfig> getConfig() {
        AiConfig config = aiConfigService.getOrCreateConfig();
        AiConfig copy = new AiConfig();
        org.springframework.beans.BeanUtils.copyProperties(config, copy);
        if (copy.getApiKey() != null && !copy.getApiKey().isBlank()) {
            copy.setApiKey("<CONFIGURED>");
        }
        return Result.success(copy);
    }

    @PostMapping("/config/save")
    public Result<AiConfig> saveConfig(@RequestBody AiConfig config) {
        if (!"admin".equals(AuthUtils.getCurrentUserRole())) {
            return Result.error(403, "仅管理员允许修改 AI 助手配置");
        }
        if (config.getId() == null) {
            config.setId(1L);
        }
        AiConfig existing = aiConfigService.getById(config.getId());
        if (existing != null && (config.getApiKey() == null || config.getApiKey().isBlank() || "<CONFIGURED>".equals(config.getApiKey()))) {
            config.setApiKey(existing.getApiKey());
        }
        config.setUpdateTime(LocalDateTime.now());
        aiConfigService.saveOrUpdate(config);
        AiConfig copy = new AiConfig();
        org.springframework.beans.BeanUtils.copyProperties(config, copy);
        if (copy.getApiKey() != null && !copy.getApiKey().isBlank()) {
            copy.setApiKey("<CONFIGURED>");
        }
        return Result.success(copy);
    }
}
