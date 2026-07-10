package com.dorm.backend.service.impl;

import com.dorm.backend.entity.Room;
import com.dorm.backend.mapper.RoomMapper;
import com.dorm.backend.service.RoomService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> implements RoomService {
}
