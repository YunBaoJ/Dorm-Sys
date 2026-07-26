package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.entity.BusinessRecord;
import com.dorm.backend.service.BusinessRecordService;
import com.dorm.backend.service.DormManagerScopeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    void studentReceivesPublishedAdminNoticesWithOnlyPublicFields() {
        BusinessRecordService service = mock(BusinessRecordService.class);
        BusinessRecord draft = notice("admin_notice", "草稿");
        draft.setId(8L);
        BusinessRecord published = notice("admin_notice", "已发布");
        published.setId(9L);
        published.setOwner("全体学生");
        published.setCreatorId(1L);
        published.setReply("内部处理备注");
        published.setEventTime(LocalDateTime.of(2026, 7, 21, 11, 0));
        published.setUpdateTime(LocalDateTime.of(2026, 7, 21, 12, 0));
        when(service.list(org.mockito.ArgumentMatchers.<Wrapper<BusinessRecord>>any()))
                .thenReturn(List.of(draft, published));
        org.mockito.Mockito.doReturn(List.of(published)).when(service).list(
                org.mockito.ArgumentMatchers.argThat(queryWithParameters("admin_notice", "已发布")));
        BusinessRecordController controller = controller(service);
        authenticate(7L, "student");

        Result<?> result = controller.list("admin_notice", "草稿");

        assertThat(result.getCode()).isEqualTo(200);
        JsonNode notice = new ObjectMapper().findAndRegisterModules().valueToTree(result.getData()).get(0);
        Set<String> fields = new HashSet<>();
        notice.fieldNames().forEachRemaining(fields::add);
        assertThat(fields).containsExactlyInAnyOrder(
                "id", "title", "owner", "description", "status", "eventTime", "createTime");
        assertThat(notice.get("id").asLong()).isEqualTo(9L);
        assertThat(notice.get("status").asText()).isEqualTo("已发布");
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
    void editingPublishedAdminNoticePreservesFirstEventTime() {
        BusinessRecordService service = mock(BusinessRecordService.class);
        LocalDateTime firstPublishedAt = LocalDateTime.of(2026, 7, 20, 9, 30);
        BusinessRecord existing = notice("admin_notice", "已发布");
        existing.setId(9L);
        existing.setEventTime(firstPublishedAt);
        when(service.getById(9L)).thenReturn(existing);
        when(service.saveOrUpdate(any())).thenReturn(true);
        BusinessRecordController controller = controller(service);
        authenticate(1L, "admin");

        BusinessRecord edited = notice("admin_notice", "已发布");
        edited.setId(9L);
        edited.setEventTime(LocalDateTime.of(2030, 1, 1, 0, 0));

        assertThat(controller.save(edited).getCode()).isEqualTo(200);
        ArgumentCaptor<BusinessRecord> captor = ArgumentCaptor.forClass(BusinessRecord.class);
        verify(service).saveOrUpdate(captor.capture());
        assertThat(captor.getValue().getEventTime()).isEqualTo(firstPublishedAt);
    }

    @Test
    void publishingDraftAdminNoticeSetsFirstEventTime() {
        BusinessRecordService service = mock(BusinessRecordService.class);
        BusinessRecord existing = notice("admin_notice", "草稿");
        existing.setId(10L);
        when(service.getById(10L)).thenReturn(existing);
        when(service.saveOrUpdate(any())).thenReturn(true);
        BusinessRecordController controller = controller(service);
        authenticate(1L, "admin");

        BusinessRecord published = notice("admin_notice", "已发布");
        published.setId(10L);

        assertThat(controller.save(published).getCode()).isEqualTo(200);
        ArgumentCaptor<BusinessRecord> captor = ArgumentCaptor.forClass(BusinessRecord.class);
        verify(service).saveOrUpdate(captor.capture());
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
    void listReturnsRecordsForRequestedType() {
        BusinessRecordService service = mock(BusinessRecordService.class);
        BusinessRecord feedback = notice("feedback", "PENDING");
        BusinessRecord managerCall = notice("manager_call", "待呼叫");
        when(service.list(org.mockito.ArgumentMatchers.<Wrapper<BusinessRecord>>any()))
                .thenReturn(List.of(feedback, managerCall));
        org.mockito.Mockito.doReturn(List.of(feedback)).when(service).list(
                org.mockito.ArgumentMatchers.argThat(queryWithParameters("feedback")));
        BusinessRecordController controller = controller(service);

        Result<?> result = controller.list("feedback", null);

        assertThat(result.getData()).isEqualTo(List.of(feedback));
    }

    @Test
    void studentReceivesOnlyOwnFeedbackRecords() {
        BusinessRecordService service = mock(BusinessRecordService.class);
        BusinessRecord ownFeedback = notice("feedback", "PENDING");
        ownFeedback.setCreatorId(7L);
        BusinessRecord otherFeedback = notice("feedback", "PENDING");
        otherFeedback.setCreatorId(8L);
        when(service.list(org.mockito.ArgumentMatchers.<Wrapper<BusinessRecord>>any()))
                .thenReturn(List.of(ownFeedback, otherFeedback));
        org.mockito.Mockito.doReturn(List.of(ownFeedback)).when(service).list(
                org.mockito.ArgumentMatchers.argThat(queryWithParameters("feedback", 7L)));
        BusinessRecordController controller = controller(service);
        authenticate(7L, "student");

        Result<?> result = controller.list("feedback", null);

        assertThat(result.getData()).isEqualTo(List.of(ownFeedback));
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

    private ArgumentMatcher<Wrapper<BusinessRecord>> queryWithParameters(Object... expectedValues) {
        return wrapper -> {
            if (!(wrapper instanceof QueryWrapper<?> queryWrapper)) return false;
            queryWrapper.getSqlSegment();
            return Arrays.stream(expectedValues)
                    .allMatch(queryWrapper.getParamNameValuePairs().values()::contains);
        };
    }
}
