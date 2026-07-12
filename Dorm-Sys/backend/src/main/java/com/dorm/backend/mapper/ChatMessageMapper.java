package com.dorm.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dorm.backend.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
