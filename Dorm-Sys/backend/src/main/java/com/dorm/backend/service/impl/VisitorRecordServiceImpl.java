package com.dorm.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.entity.VisitorRecord;
import com.dorm.backend.entity.User;
import com.dorm.backend.mapper.VisitorRecordMapper;
import com.dorm.backend.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VisitorRecordServiceImpl extends ServiceImpl<VisitorRecordMapper, VisitorRecord> implements VisitorRecordService {

    private final UserService userService;
    private final DormManagerScopeService managerScopeService;

    public VisitorRecordServiceImpl(UserService userService, DormManagerScopeService managerScopeService) {
        this.userService = userService;
        this.managerScopeService = managerScopeService;
    }

    @Override
    public List<VisitorRecord> listVisitorRecordsWithDetails(Long studentId, String role, Long userId) {
        QueryWrapper<VisitorRecord> qw = new QueryWrapper<>();
        if ("student".equals(role)) studentId = userId;
        if ("dormmanager".equals(role)) {
            List<Long> studentIds = managerScopeService.managedStudentIds(userId);
            if (studentIds.isEmpty() || (studentId != null && !studentIds.contains(studentId))) return List.of();
            if (studentId == null) qw.in("student_id", studentIds);
        }
        if (studentId != null) qw.eq("student_id", studentId);
        qw.orderByDesc("create_time");

        List<VisitorRecord> list = this.list(qw);
        if (list.isEmpty()) return list;

        // 按需加载学生姓名
        Set<Long> studentIds = list.stream().map(VisitorRecord::getStudentId).collect(Collectors.toSet());
        Map<Long, String> userMap = studentIds.isEmpty() ? Collections.emptyMap()
                : userService.listByIds(studentIds).stream()
                    .collect(Collectors.toMap(User::getId, User::getName));

        for (VisitorRecord vr : list) {
            vr.setStudentName(userMap.get(vr.getStudentId()));
        }
        return list;
    }
}
