package com.dorm.backend.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dorm.backend.entity.StudentInfo;
import com.dorm.backend.mapper.StudentInfoMapper;
import com.dorm.backend.service.StudentInfoService;
import org.springframework.stereotype.Service;

@Service
public class StudentInfoServiceImpl extends ServiceImpl<StudentInfoMapper, StudentInfo> implements StudentInfoService {}