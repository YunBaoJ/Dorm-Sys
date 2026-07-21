package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.entity.BusinessRecord;
import com.dorm.backend.service.BusinessRecordService;
import com.dorm.backend.service.DormManagerScopeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BusinessRecordControllerTest {

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void studentCanReadOnlyPublishedAdminNotices() {
        BusinessRecordService service = mock(BusinessRecordService.class);
        BusinessRecordController controller = controller(service);
        authenticate(7L, "student");

        Result<java.util.List<BusinessRecord>> result = controller.list("admin_notice", null);

        assertThat(result.getCode()).isEqualTo(200);
        ArgumentCaptor<QueryWrapper<BusinessRecord>> captor = ArgumentCaptor.forClass((Class) QueryWrapper.class);
        verify(service).list(captor.capture());
        assertThat(captor.getValue().getSqlSegment()).contains("type");
        assertThat(captor.getValue().getSqlSegment()).contains("status");
        assertThat(captor.getValue().getParamNameValuePairs()).containsValue("admin_notice");
        assertThat(captor.getValue().getParamNameValuePairs()).containsValue("已发布");
    }

    @Test
    void managerCannotPublishAdminNotice() {
        BusinessRecordService service = mock(BusinessRecordService.class);
        BusinessRecordController controller = controller(service);
        authenticate(8L, "dormmanager");

        BusinessRecord notice = notice("admin_notice", "已发布");

        assertThat(controller.save(notice).getCode()).isEqualTo(403);
        verify(service, never()).saveOrUpdate(any());
    }

    @Test
    void managerCannotDeleteAdminNotice() {
        BusinessRecordService service = mock(BusinessRecordService.class);
        when(service.getById(9L)).thenReturn(notice("admin_notice", "已发布"));
        BusinessRecordController controller = controller(service);
        authenticate(8L, "dormmanager");

        assertThat(controller.delete(9L).getCode()).isEqualTo(403);
        verify(service, never()).removeById((java.io.Serializable) 9L);
    }

    @Test
    void adminCanSavePublishedAdminNotice() {
        BusinessRecordService service = mock(BusinessRecordService.class);
        when(service.saveOrUpdate(any())).thenReturn(true);
        BusinessRecordController controller = controller(service);
        authenticate(1L, "admin");

        BusinessRecord notice = notice("admin_notice", "已发布");

        assertThat(controller.save(notice).getCode()).isEqualTo(200);
        ArgumentCaptor<BusinessRecord> captor = ArgumentCaptor.forClass(BusinessRecord.class);
        verify(service).saveOrUpdate(captor.capture());
        assertThat(captor.getValue().getCreatorId()).isEqualTo(1L);
        assertThat(captor.getValue().getEventTime()).isNotNull();
    }

    @Test
    void adminNoticeOnlyAcceptsDraftOrPublishedStatus() {
        BusinessRecordService service = mock(BusinessRecordService.class);
        BusinessRecordController controller = controller(service);
        authenticate(1L, "admin");

        BusinessRecord notice = notice("admin_notice", "归档");

        assertThat(controller.save(notice).getCode()).isEqualTo(400);
        verify(service, never()).saveOrUpdate(any());
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void listFiltersByType() {
        BusinessRecordService service = mock(BusinessRecordService.class);
        BusinessRecordController controller = new BusinessRecordController();
        ReflectionTestUtils.setField(controller, "businessRecordService", service);

        controller.list("feedback", null);

        ArgumentCaptor<QueryWrapper<BusinessRecord>> captor = ArgumentCaptor.forClass((Class) QueryWrapper.class);
        verify(service).list(captor.capture());
        assertThat(captor.getValue().getSqlSegment()).contains("type");
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void studentRecordListIsScopedToCurrentUser() {
        BusinessRecordService service = mock(BusinessRecordService.class);
        BusinessRecordController controller = new BusinessRecordController();
        ReflectionTestUtils.setField(controller, "businessRecordService", service);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("currentUserId", 7L);
        request.setAttribute("currentUserRole", "student");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        controller.list("feedback", null);

        ArgumentCaptor<QueryWrapper<BusinessRecord>> captor = ArgumentCaptor.forClass((Class) QueryWrapper.class);
        verify(service).list(captor.capture());
        assertThat(captor.getValue().getSqlSegment()).contains("creator_id");
        assertThat(captor.getValue().getParamNameValuePairs()).containsValue(7L);
    }

    @Test
    void studentCannotCreateManagerMessage() {
        BusinessRecordService service = mock(BusinessRecordService.class);
        BusinessRecordController controller = new BusinessRecordController();
        ReflectionTestUtils.setField(controller, "businessRecordService", service);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("currentUserId", 7L);
        request.setAttribute("currentUserRole", "student");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        BusinessRecord record = new BusinessRecord();
        record.setType("manager_messages");
        record.setTitle("伪造公告");

        assertThat(controller.save(record).getCode()).isEqualTo(403);
        verify(service, never()).saveOrUpdate(any());
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
        BusinessRecordController controller = controller(service);

        BusinessRecord record = new BusinessRecord();
        record.setTitle("联系学生");

        assertThat(controller.save(record).getCode()).isEqualTo(400);
        verify(service, never()).saveOrUpdate(any());
    }

    private BusinessRecordController controller(BusinessRecordService service) {
        BusinessRecordController controller = new BusinessRecordController();
        ReflectionTestUtils.setField(controller, "businessRecordService", service);
        ReflectionTestUtils.setField(controller, "managerScopeService", mock(DormManagerScopeService.class));
        return controller;
    }

    private void authenticate(Long userId, String role) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("currentUserId", userId);
        request.setAttribute("currentUserRole", role);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private BusinessRecord notice(String type, String status) {
        BusinessRecord record = new BusinessRecord();
        record.setType(type);
        record.setTitle("校园通知");
        record.setStatus(status);
        record.setDescription("测试公告");
        record.setCreateTime(LocalDateTime.of(2026, 7, 21, 10, 0));
        return record;
    }
}
