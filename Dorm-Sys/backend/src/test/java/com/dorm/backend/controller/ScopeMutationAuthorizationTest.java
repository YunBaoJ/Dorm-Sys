package com.dorm.backend.controller;

import com.dorm.backend.entity.Bed;
import com.dorm.backend.entity.FeeBill;
import com.dorm.backend.entity.TransferRequest;
import com.dorm.backend.service.BedService;
import com.dorm.backend.service.DormManagerScopeService;
import com.dorm.backend.service.FeeBillService;
import com.dorm.backend.service.TransferRequestService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScopeMutationAuthorizationTest {

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void managerCannotMoveBedFromAnotherBuildingByChangingRoomId() {
        authenticateManager(7L);
        BedService bedService = mock(BedService.class);
        DormManagerScopeService scopeService = mock(DormManagerScopeService.class);
        Bed existing = new Bed();
        existing.setId(5L);
        existing.setRoomId(99L);
        when(bedService.getById(5L)).thenReturn(existing);
        when(scopeService.canManageRoom(7L, 99L)).thenReturn(false);
        when(scopeService.canManageRoom(7L, 1L)).thenReturn(true);

        BedController controller = new BedController();
        ReflectionTestUtils.setField(controller, "bedService", bedService);
        ReflectionTestUtils.setField(controller, "managerScopeService", scopeService);

        Bed submitted = new Bed();
        submitted.setId(5L);
        submitted.setRoomId(1L);

        assertThat(controller.save(submitted).getCode()).isEqualTo(403);
        verify(bedService, never()).update(any());
    }

    @Test
    void managerCannotMoveAnotherBuildingsBillIntoManagedRoom() {
        authenticateManager(7L);
        FeeBillService feeBillService = mock(FeeBillService.class);
        DormManagerScopeService scopeService = mock(DormManagerScopeService.class);
        FeeBill existing = new FeeBill();
        existing.setId(8L);
        existing.setRoomId(99L);
        when(feeBillService.getById(8L)).thenReturn(existing);
        when(scopeService.canManageRoom(7L, 99L)).thenReturn(false);

        FeeBillController controller = new FeeBillController();
        ReflectionTestUtils.setField(controller, "feeBillService", feeBillService);
        ReflectionTestUtils.setField(controller, "managerScopeService", scopeService);

        FeeBill submitted = new FeeBill();
        submitted.setId(8L);
        submitted.setRoomId(1L);

        assertThat(controller.save(submitted).getCode()).isEqualTo(403);
        verify(feeBillService, never()).saveOrUpdate(any());
    }

    @Test
    void managerCannotApproveTransferIntoAnotherBuilding() {
        authenticateManager(7L);
        TransferRequestService transferService = mock(TransferRequestService.class);
        BedService bedService = mock(BedService.class);
        DormManagerScopeService scopeService = mock(DormManagerScopeService.class);

        TransferRequest existing = new TransferRequest();
        existing.setId(10L);
        existing.setStudentId(3L);
        existing.setCurrentBedId(4L);
        existing.setStatus("PENDING");
        Bed currentBed = new Bed();
        currentBed.setId(4L);
        currentBed.setRoomId(1L);
        when(transferService.getById(10L)).thenReturn(existing);
        when(bedService.getById(4L)).thenReturn(currentBed);
        when(scopeService.canManageRoom(7L, 1L)).thenReturn(true);
        when(scopeService.canManageRoom(7L, 99L)).thenReturn(false);

        TransferRequestController controller = new TransferRequestController();
        ReflectionTestUtils.setField(controller, "transferRequestService", transferService);
        ReflectionTestUtils.setField(controller, "bedService", bedService);
        ReflectionTestUtils.setField(controller, "managerScopeService", scopeService);

        TransferRequest submitted = new TransferRequest();
        submitted.setId(10L);
        submitted.setStudentId(999L);
        submitted.setCurrentBedId(999L);
        submitted.setTargetRoomId(99L);
        submitted.setStatus("APPROVED");

        assertThat(controller.save(submitted).getCode()).isEqualTo(403);
        verify(transferService, never()).saveOrUpdate(any());
    }

    private void authenticateManager(Long userId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("currentUserId", userId);
        request.setAttribute("currentUserRole", "dormmanager");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
}
