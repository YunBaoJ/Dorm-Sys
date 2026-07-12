package com.dorm.backend.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.backend.entity.AdminInfo;
import com.dorm.backend.mapper.AdminInfoMapper;
import com.dorm.backend.service.AdminInfoService;
import org.springframework.stereotype.Service;

@Service
public class AdminInfoServiceImpl extends ServiceImpl<AdminInfoMapper, AdminInfo> implements AdminInfoService {}