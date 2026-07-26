package com.dorm.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.entity.Bed;
import com.dorm.backend.entity.User;
import com.dorm.backend.mapper.BedMapper;
import com.dorm.backend.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BedServiceImpl extends ServiceImpl<BedMapper, Bed> implements BedService {

    private final UserService userService;
    private final DormManagerScopeService managerScopeService;

    public BedServiceImpl(UserService userService, DormManagerScopeService managerScopeService) {
        this.userService = userService;
        this.managerScopeService = managerScopeService;
    }

    @Override
    public List<Bed> listBedsWithDetails(Long roomId, String status, String role, Long userId) {
        QueryWrapper<Bed> queryWrapper = new QueryWrapper<>();
        if ("dormmanager".equals(role)) {
            List<Long> roomIds = managerScopeService.managedRoomIds(userId);
            if (roomIds.isEmpty() || (roomId != null && !roomIds.contains(roomId))) return List.of();
            if (roomId == null) queryWrapper.in("room_id", roomIds);
            else queryWrapper.eq("room_id", roomId);
        } else if (roomId != null) {
            queryWrapper.eq("room_id", roomId);
        }
        if (status != null) queryWrapper.eq("status", status);

        List<Bed> beds = this.list(queryWrapper);
        if (beds.isEmpty()) return beds;

        // 按需加载学生姓名
        Set<Long> studentIds = beds.stream().map(Bed::getStudentId)
                .filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> studentMap = studentIds.isEmpty() ? Collections.emptyMap()
                : userService.listByIds(studentIds).stream()
                    .collect(Collectors.toMap(User::getId, User::getName));

        for (Bed bed : beds) {
            if (bed.getStudentId() != null) {
                bed.setStudentName(studentMap.get(bed.getStudentId()));
            }
        }
        return beds;
    }
}
