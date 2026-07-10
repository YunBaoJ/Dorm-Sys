package com.dorm.backend.controller;

import com.dorm.backend.entity.Room;
import com.dorm.backend.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/room")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @GetMapping("/list")
    public List<Room> list() {
        return roomService.list();
    }

    @GetMapping("/{id}")
    public Room getById(@PathVariable Long id) {
        return roomService.getById(id);
    }

    @PostMapping("/save")
    public boolean save(@RequestBody Room room) {
        return roomService.saveOrUpdate(room);
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return roomService.removeById(id);
    }
}
