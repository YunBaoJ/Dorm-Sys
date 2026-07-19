package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.entity.*;
import com.dorm.backend.service.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StudentDataOwnershipTest {

    @BeforeEach
    void setStudentRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("currentUserId", 7L);
        request.setAttribute("currentUserRole", "student");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void clearRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void studentCannotReadAnotherStudentsRepair() {
        RepairRequestService service = mock(RepairRequestService.class);
        RepairRequest record = new RepairRequest();
        record.setSubmitterId(8L);
        when(service.getById(1L)).thenReturn(record);
        RepairRequestController controller = new RepairRequestController();
        ReflectionTestUtils.setField(controller, "repairRequestService", service);

        Result<RepairRequest> result = controller.getById(1L);

        assertThat(result.getCode()).isEqualTo(403);
    }

    @Test
    void studentCannotReadAnotherStudentsTransfer() {
        TransferRequestService service = mock(TransferRequestService.class);
        TransferRequest record = new TransferRequest();
        record.setStudentId(8L);
        when(service.getById(1L)).thenReturn(record);
        TransferRequestController controller = new TransferRequestController();
        ReflectionTestUtils.setField(controller, "transferRequestService", service);

        Result<TransferRequest> result = controller.getById(1L);

        assertThat(result.getCode()).isEqualTo(403);
    }

    @Test
    void studentCannotReadAnotherStudentsVisitor() {
        VisitorRecordService service = mock(VisitorRecordService.class);
        VisitorRecord record = new VisitorRecord();
        record.setStudentId(8L);
        when(service.getById(1L)).thenReturn(record);
        VisitorRecordController controller = new VisitorRecordController();
        ReflectionTestUtils.setField(controller, "visitorRecordService", service);

        Result<VisitorRecord> result = controller.getById(1L);

        assertThat(result.getCode()).isEqualTo(403);
    }

    @Test
    void studentFeeListIgnoresAnotherRoomId() {
        FeeBillService feeService = mock(FeeBillService.class);
        BedService bedService = mock(BedService.class);
        Bed currentBed = new Bed();
        currentBed.setRoomId(10L);
        when(bedService.getOne(any(Wrapper.class))).thenReturn(currentBed);
        when(feeService.list(any(Wrapper.class))).thenReturn(List.of());

        FeeBillController controller = new FeeBillController();
        ReflectionTestUtils.setField(controller, "feeBillService", feeService);
        ReflectionTestUtils.setField(controller, "bedService", bedService);
        ReflectionTestUtils.setField(controller, "roomService", mock(RoomService.class));
        ReflectionTestUtils.setField(controller, "buildingService", mock(BuildingService.class));

        controller.list(99L, null);

        org.mockito.ArgumentCaptor<QueryWrapper<FeeBill>> captor = org.mockito.ArgumentCaptor.forClass(QueryWrapper.class);
        org.mockito.Mockito.verify(feeService).list(captor.capture());
        assertThat(captor.getValue().getExpression().getNormal().getSqlSegment()).contains("room_id");
        assertThat(captor.getValue().getParamNameValuePairs()).containsValue(10L).doesNotContainValue(99L);
    }
}
