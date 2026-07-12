package com.dorm.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dorm.backend.entity.CallRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CallRecordMapper extends BaseMapper<CallRecord> {
}
