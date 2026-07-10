package com.dorm.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dorm.backend.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
