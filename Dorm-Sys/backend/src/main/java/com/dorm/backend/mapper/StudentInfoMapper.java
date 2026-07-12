package com.dorm.backend.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dorm.backend.entity.StudentInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StudentInfoMapper extends BaseMapper<StudentInfo> {}