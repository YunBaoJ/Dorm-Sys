package com.dorm.backend.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.backend.entity.ManagerInfo;
import com.dorm.backend.mapper.ManagerInfoMapper;
import com.dorm.backend.service.ManagerInfoService;
import org.springframework.stereotype.Service;

@Service
public class ManagerInfoServiceImpl extends ServiceImpl<ManagerInfoMapper, ManagerInfo> implements ManagerInfoService {}