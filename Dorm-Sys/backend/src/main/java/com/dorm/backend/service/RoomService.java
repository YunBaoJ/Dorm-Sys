package com.dorm.backend.service;

import com.dorm.backend.entity.Room;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface RoomService extends IService<Room> {
    List<Room> listRoomsWithDetails(Long buildingId, String role, Long userId);
}
