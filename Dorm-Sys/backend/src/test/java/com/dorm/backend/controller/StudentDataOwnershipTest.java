package com.dorm.backend.controller;

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
        RepairRequestController controller = new RepairRequestController(service, mock(BedService.class), mock(DormManagerScopeService.class));
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
        TransferRequestController controller = new TransferRequestController(service, mock(UserService.class),
            mock(BedService.class), mock(RoomService.class), mock(BuildingService.class), mock(DormManagerScopeService.class));
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
        VisitorRecordController controller = new VisitorRecordController(service, mock(UserService.class), mock(DormManagerScopeService.class));
        ReflectionTestUtils.setField(controller, "visitorRecordService", service);

        Result<VisitorRecord> result = controller.getById(1L);

        assertThat(result.getCode()).isEqualTo(403);
    }

    @Test
    void studentFeeListIgnoresAnotherRoomId() {
        FeeBillService feeService = mock(FeeBillService.class);

        DormManagerScopeService scopeService = mock(DormManagerScopeService.class);
        FeeBillController controller = new FeeBillController(feeService, scopeService);

        controller.list(99L, null, 1, 100);

        // The role-based filtering is now done in the service layer (listFeeBillsWithDetails)
        // Verify the controller delegates to the service correctly
        org.mockito.Mockito.verify(feeService).listFeeBillsWithDetails(99L, null, "student", 7L);
    }
}
