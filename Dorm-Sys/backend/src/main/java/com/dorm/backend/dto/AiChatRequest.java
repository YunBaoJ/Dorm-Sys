package com.dorm.backend.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AiChatRequest {
    private String question;
    private List<Map<String, String>> history;
}
