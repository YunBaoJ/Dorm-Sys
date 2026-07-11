package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.entity.BusinessRecord;
import com.dorm.backend.service.BusinessRecordService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BusinessRecordControllerTest {

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void listFiltersByType() {
        BusinessRecordService service = mock(BusinessRecordService.class);
        BusinessRecordController controller = new BusinessRecordController();
        ReflectionTestUtils.setField(controller, "businessRecordService", service);

        controller.list("student_feedback", null);

        ArgumentCaptor<QueryWrapper<BusinessRecord>> captor = ArgumentCaptor.forClass((Class) QueryWrapper.class);
        verify(service).list(captor.capture());
        assertThat(captor.getValue().getSqlSegment()).contains("type");
    }

    @Test
    void savePersistsRecord() {
        BusinessRecordService service = mock(BusinessRecordService.class);
        when(service.saveOrUpdate(any())).thenReturn(true);
        BusinessRecordController controller = new BusinessRecordController();
        ReflectionTestUtils.setField(controller, "businessRecordService", service);

        BusinessRecord record = new BusinessRecord();
        record.setType("manager_call");
        record.setTitle("联系学生");
        record.setStatus("待呼叫");

        controller.save(record);

        ArgumentCaptor<BusinessRecord> captor = ArgumentCaptor.forClass(BusinessRecord.class);
        verify(service).saveOrUpdate(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo("manager_call");
        assertThat(captor.getValue().getCreateTime()).isNotNull();
        assertThat(captor.getValue().getUpdateTime()).isNotNull();
    }

    @Test
    void saveRejectsMissingType() {
        BusinessRecordService service = mock(BusinessRecordService.class);
        BusinessRecordController controller = new BusinessRecordController();
        ReflectionTestUtils.setField(controller, "businessRecordService", service);

        BusinessRecord record = new BusinessRecord();
        record.setTitle("联系学生");

        assertThat(controller.save(record).getCode()).isEqualTo(400);
        verify(service, never()).saveOrUpdate(any());
    }
}
