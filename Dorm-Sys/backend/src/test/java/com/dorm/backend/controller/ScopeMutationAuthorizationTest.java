package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.dorm.backend.entity.Bed;
import com.dorm.backend.entity.FeeBill;
import com.dorm.backend.entity.Room;
import com.dorm.backend.entity.TransferRequest;
import com.dorm.backend.service.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

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

        BedController controller = new BedController(bedService, mock(UserService.class), mock(StayHistoryService.class),
            mock(RoomService.class), scopeService);
        ReflectionTestUtils.setField(controller, "bedService", bedService);
        ReflectionTestUtils.setField(controller, "managerScopeService", scopeService);

        Bed submitted = new Bed();
        submitted.setId(5L);
        submitted.setRoomId(1L);

        assertThat(controller.save(submitted).getCode()).isEqualTo(403);
        verify(bedService, never()).update(any());
    }

    @Test
    void managerCannotTargetAnotherBuildingWhenBedMovesAreRejected() {
        authenticateManager(7L);
        BedService bedService = mock(BedService.class);
        DormManagerScopeService scopeService = mock(DormManagerScopeService.class);
        Bed existing = new Bed();
        existing.setId(5L);
        existing.setRoomId(1L);
        when(bedService.getById(5L)).thenReturn(existing);
        when(scopeService.canManageRoom(7L, 1L)).thenReturn(true);
        when(scopeService.canManageRoom(7L, 99L)).thenReturn(false);

        BedController controller = new BedController(bedService, mock(UserService.class), mock(StayHistoryService.class),
            mock(RoomService.class), scopeService);
        Bed submitted = new Bed();
        submitted.setId(5L);
        submitted.setRoomId(99L);

        assertThat(controller.save(submitted).getCode()).isEqualTo(403);
        verify(bedService, never()).update(any());
    }

    @Test
    void managerCannotCreateBedDirectlyInAnotherBuilding() {
        authenticateManager(7L);
        BedService bedService = mock(BedService.class);
        DormManagerScopeService scopeService = mock(DormManagerScopeService.class);
        when(scopeService.canManageRoom(7L, 99L)).thenReturn(false);

        BedController controller = new BedController(bedService, mock(UserService.class), mock(StayHistoryService.class),
            mock(RoomService.class), scopeService);
        Bed submitted = new Bed();
        submitted.setRoomId(99L);

        assertThat(controller.save(submitted).getCode()).isEqualTo(403);
        verify(bedService, never()).save(any());
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
        when(scopeService.canManageRoom(7L, 1L)).thenReturn(true);
        RoomService roomService = mock(RoomService.class);
        Room targetRoom = new Room();
        targetRoom.setId(1L);
        when(roomService.getOne(any(Wrapper.class))).thenReturn(targetRoom);

        FeeBillController controller = new FeeBillController(feeBillService, roomService, scopeService);
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
        currentBed.setStudentId(3L);
        when(transferService.getOne(any(Wrapper.class))).thenReturn(existing);
        when(bedService.getById(4L)).thenReturn(currentBed);
        when(scopeService.canManageRoom(7L, 1L)).thenReturn(true);
        when(scopeService.canManageRoom(7L, 99L)).thenReturn(false);

        TransferRequestController controller = new TransferRequestController(transferService, validUserService(),
            bedService, mock(RoomService.class), mock(BuildingService.class), mock(StayHistoryService.class), scopeService);
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

    @Test
    void managerCannotUseStaleRequestBedToMoveStudentFromAnotherBuilding() {
        authenticateManager(7L);
        TransferRequestService transferService = mock(TransferRequestService.class);
        BedService bedService = mock(BedService.class);
        RoomService roomService = mock(RoomService.class);
        StayHistoryService historyService = mock(StayHistoryService.class);
        DormManagerScopeService scopeService = mock(DormManagerScopeService.class);

        TransferRequest existing = new TransferRequest();
        existing.setId(10L);
        existing.setStudentId(3L);
        existing.setCurrentBedId(4L);
        existing.setStatus("PENDING");
        Bed staleRequestBed = new Bed();
        staleRequestBed.setId(4L);
        staleRequestBed.setRoomId(1L);
        staleRequestBed.setStudentId(99L);
        staleRequestBed.setStatus("OCCUPIED");
        Bed actualStudentBed = new Bed();
        actualStudentBed.setId(5L);
        actualStudentBed.setRoomId(99L);
        actualStudentBed.setStudentId(3L);
        actualStudentBed.setStatus("OCCUPIED");
        Bed targetBed = new Bed();
        targetBed.setId(6L);
        targetBed.setRoomId(2L);
        targetBed.setStatus("EMPTY");
        Room targetRoom = new Room();
        targetRoom.setId(2L);
        targetRoom.setCapacity(1);
        targetRoom.setStatus("NORMAL");
        Room actualRoom = new Room();
        actualRoom.setId(99L);
        actualRoom.setCapacity(1);
        actualRoom.setStatus("FULL");

        when(transferService.getOne(any(Wrapper.class))).thenReturn(existing);
        when(transferService.saveOrUpdate(any())).thenReturn(true);
        when(bedService.getById(4L)).thenReturn(staleRequestBed);
        when(bedService.list(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any()))
            .thenReturn(List.of(actualStudentBed), List.of(actualStudentBed), List.of(targetBed),
                List.of(actualStudentBed), List.of(targetBed));
        when(bedService.update(any())).thenReturn(true);
        when(roomService.list(org.mockito.ArgumentMatchers.<Wrapper<Room>>any()))
            .thenReturn(List.of(targetRoom, actualRoom));
        when(roomService.updateById(any())).thenReturn(true);
        when(historyService.save(any())).thenReturn(true);
        when(scopeService.canManageRoom(7L, 1L)).thenReturn(true);
        when(scopeService.canManageRoom(7L, 2L)).thenReturn(true);
        when(scopeService.canManageRoom(7L, 99L)).thenReturn(false);

        TransferRequestController controller = new TransferRequestController(transferService, validUserService(),
            bedService, roomService, mock(BuildingService.class), historyService, scopeService);
        TransferRequest submitted = new TransferRequest();
        submitted.setId(10L);
        submitted.setTargetRoomId(2L);
        submitted.setStatus("APPROVED");

        assertThat(controller.save(submitted).getCode()).isEqualTo(403);
        verify(bedService, never()).update(any());
        verify(transferService, never()).saveOrUpdate(any());
    }

    private void authenticateManager(Long userId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("currentUserId", userId);
        request.setAttribute("currentUserRole", "dormmanager");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private UserService validUserService() {
        UserService userService = mock(UserService.class);
        when(userService.getOne(any(Wrapper.class))).thenReturn(new com.dorm.backend.entity.User());
        return userService;
    }
}
