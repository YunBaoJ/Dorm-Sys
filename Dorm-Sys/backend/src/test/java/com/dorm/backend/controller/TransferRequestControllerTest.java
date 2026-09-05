package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.common.BedAllocationConflictException;
import com.dorm.backend.entity.Bed;
import com.dorm.backend.entity.Room;
import com.dorm.backend.entity.StayHistory;
import com.dorm.backend.entity.TransferRequest;
import com.dorm.backend.service.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransferRequestControllerTest {

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void approveRejectsMaintenanceTargetRoom() {
        TransferRequestService transferRequestService = mock(TransferRequestService.class);
        BedService bedService = mock(BedService.class);
        RoomService roomService = mock(RoomService.class);
        StayHistoryService historyService = mock(StayHistoryService.class);

        Bed currentBed = bed(1L, 1L, 7L, "OCCUPIED");
        Bed targetBed = bed(2L, 2L, null, "EMPTY");
        Room maintenanceRoom = room(2L, 1, "MAINTENANCE");
        when(bedService.getById(1L)).thenReturn(currentBed);
        when(bedService.getOne(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any())).thenReturn(currentBed);
        when(bedService.list(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any()))
            .thenAnswer(invocation -> bedsForRoom(invocation.getArgument(0), currentBed, targetBed));
        stubLockedRooms(roomService, room(1L, 1, "NORMAL"), maintenanceRoom);
        when(roomService.getById(org.mockito.ArgumentMatchers.anyLong())).thenReturn(maintenanceRoom);
        when(transferRequestService.saveOrUpdate(any())).thenReturn(true);

        TransferRequestController controller = new TransferRequestController(transferRequestService, validUserService(),
            bedService, roomService, mock(BuildingService.class), historyService, mock(DormManagerScopeService.class));

        Result<Boolean> result = controller.save(approvedRequest(7L, 1L, 2L));

        org.assertj.core.api.Assertions.assertThat(result.getCode()).isEqualTo(400);
        verify(bedService, org.mockito.Mockito.never()).updateById(any());
    }

    @Test
    void approveDoesNotClearMaintenanceStatusOfPreviousRoom() {
        TransferRequestService transferRequestService = mock(TransferRequestService.class);
        BedService bedService = mock(BedService.class);
        RoomService roomService = mock(RoomService.class);
        StayHistoryService historyService = mock(StayHistoryService.class);

        Bed currentBed = bed(1L, 1L, 7L, "OCCUPIED");
        Bed targetBed = bed(2L, 2L, null, "EMPTY");
        Room currentRoom = room(1L, 1, "MAINTENANCE");
        Room targetRoom = room(2L, 1, "NORMAL");
        when(bedService.getById(1L)).thenReturn(currentBed);
        when(bedService.getOne(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any())).thenReturn(currentBed);
        when(bedService.list(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any()))
            .thenAnswer(invocation -> bedsForRoom(invocation.getArgument(0), currentBed, targetBed));
        when(roomService.getById(1L)).thenReturn(currentRoom);
        when(roomService.getById(2L)).thenReturn(targetRoom);
        when(bedService.update(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any())).thenReturn(true);
        when(historyService.save(any(StayHistory.class))).thenReturn(true);
        stubLockedRooms(roomService, currentRoom, targetRoom);
        when(roomService.updateById(any(Room.class))).thenReturn(true);
        when(transferRequestService.saveOrUpdate(any())).thenReturn(true);

        TransferRequestController controller = new TransferRequestController(transferRequestService, validUserService(),
            bedService, roomService, mock(BuildingService.class), historyService, mock(DormManagerScopeService.class));

        Result<Boolean> result = controller.save(approvedRequest(7L, 1L, 2L));

        org.assertj.core.api.Assertions.assertThat(result.getCode()).isEqualTo(200);
        org.assertj.core.api.Assertions.assertThat(currentRoom.getStatus()).isEqualTo("MAINTENANCE");
    }

    @Test
    void approveMovesStudentToAvailableBedInTargetRoom() {
        TransferRequestService transferRequestService = mock(TransferRequestService.class);
        BedService bedService = mock(BedService.class);
        RoomService roomService = mock(RoomService.class);
        StayHistoryService historyService = mock(StayHistoryService.class);

        Bed currentBed = new Bed();
        currentBed.setId(1L);
        currentBed.setRoomId(1L);
        currentBed.setBedNumber("101-1");
        currentBed.setStudentId(1L);
        currentBed.setStatus("OCCUPIED");

        Bed targetBed = new Bed();
        targetBed.setId(5L);
        targetBed.setRoomId(2L);
        targetBed.setBedNumber("102-1");
        targetBed.setStatus("EMPTY");

        Room currentRoom = room(1L, 1, "FULL");
        Room targetRoom = room(2L, 1, "NORMAL");
        StayHistory currentHistory = new StayHistory();
        currentHistory.setId(20L);
        currentHistory.setStudentId(1L);
        currentHistory.setBedId(1L);

        when(bedService.getById(1L)).thenReturn(currentBed);
        when(bedService.getOne(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any())).thenReturn(currentBed);
        when(bedService.list(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any()))
            .thenAnswer(invocation -> bedsForRoom(invocation.getArgument(0), currentBed, targetBed));
        when(bedService.update(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any())).thenReturn(true);
        when(roomService.getById(1L)).thenReturn(currentRoom);
        when(roomService.getById(2L)).thenReturn(targetRoom);
        stubLockedRooms(roomService, currentRoom, targetRoom);
        when(roomService.updateById(any(Room.class))).thenReturn(true);
        when(historyService.getOne(org.mockito.ArgumentMatchers.<Wrapper<StayHistory>>any())).thenReturn(currentHistory);
        when(historyService.updateById(currentHistory)).thenReturn(true);
        when(historyService.save(any(StayHistory.class))).thenReturn(true);
        when(transferRequestService.saveOrUpdate(any())).thenReturn(true);

        TransferRequestController controller = new TransferRequestController(transferRequestService, validUserService(),
            bedService, roomService, mock(BuildingService.class), historyService, mock(DormManagerScopeService.class));

        TransferRequest request = new TransferRequest();
        request.setId(10L);
        request.setStudentId(1L);
        request.setCurrentBedId(1L);
        request.setTargetRoomId(2L);
        request.setStatus("APPROVED");

        controller.save(request);

        org.assertj.core.api.Assertions.assertThat(currentBed.getStudentId()).isNull();
        org.assertj.core.api.Assertions.assertThat(currentBed.getStatus()).isEqualTo("EMPTY");
        org.assertj.core.api.Assertions.assertThat(targetBed.getStudentId()).isEqualTo(1L);
        org.assertj.core.api.Assertions.assertThat(targetBed.getStatus()).isEqualTo("OCCUPIED");
        org.assertj.core.api.Assertions.assertThat(currentHistory.getCheckOutDate()).isNotNull();
        ArgumentCaptor<StayHistory> historyCaptor = ArgumentCaptor.forClass(StayHistory.class);
        verify(historyService).save(historyCaptor.capture());
        org.assertj.core.api.Assertions.assertThat(historyCaptor.getValue().getStudentId()).isEqualTo(1L);
        org.assertj.core.api.Assertions.assertThat(historyCaptor.getValue().getBedId()).isEqualTo(5L);
        org.assertj.core.api.Assertions.assertThat(historyCaptor.getValue().getCheckInDate()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(currentRoom.getStatus()).isEqualTo("NORMAL");
        org.assertj.core.api.Assertions.assertThat(targetRoom.getStatus()).isEqualTo("FULL");
        ArgumentCaptor<Wrapper<Bed>> updateCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(bedService, times(2)).update(updateCaptor.capture());
        UpdateWrapper<Bed> currentRelease = (UpdateWrapper<Bed>) updateCaptor.getAllValues().get(0);
        currentRelease.getSqlSegment();
        org.assertj.core.api.Assertions.assertThat(currentRelease.getParamNameValuePairs().values())
            .contains(1L, 1L, "OCCUPIED");
        UpdateWrapper<Bed> targetClaim = (UpdateWrapper<Bed>) updateCaptor.getAllValues().get(1);
        org.assertj.core.api.Assertions.assertThat(targetClaim.getSqlSegment())
            .contains("student_id IS NULL")
            .contains("status IS NULL")
            .contains("status =");
        org.assertj.core.api.Assertions.assertThat(targetClaim.getParamNameValuePairs().values())
            .contains(5L, 2L, "EMPTY");
        verify(bedService, never()).updateById(targetBed);
        verify(transferRequestService).saveOrUpdate(request);
    }

    @Test
    void approveLocksSourceAndTargetRoomsInIdOrderBeforeWrites() {
        TransferRequestService transferRequestService = mock(TransferRequestService.class);
        BedService bedService = mock(BedService.class);
        RoomService roomService = mock(RoomService.class);
        StayHistoryService historyService = mock(StayHistoryService.class);
        Bed currentBed = bed(1L, 20L, 7L, "OCCUPIED");
        Bed targetBed = bed(2L, 10L, null, "EMPTY");
        Room targetRoom = room(10L, 1, "NORMAL");
        Room currentRoom = room(20L, 1, "FULL");

        when(bedService.getById(1L)).thenReturn(currentBed);
        when(bedService.getOne(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any())).thenReturn(currentBed);
        when(bedService.list(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any()))
            .thenAnswer(invocation -> bedsForRoom(invocation.getArgument(0), currentBed, targetBed));
        when(roomService.list(org.mockito.ArgumentMatchers.<Wrapper<Room>>any()))
            .thenReturn(List.of(targetRoom, currentRoom));
        when(roomService.getById(10L)).thenReturn(targetRoom);
        when(roomService.getById(20L)).thenReturn(currentRoom);
        when(bedService.update(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any())).thenReturn(true);
        when(historyService.save(any(StayHistory.class))).thenReturn(true);
        when(roomService.updateById(any(Room.class))).thenReturn(true);
        when(transferRequestService.saveOrUpdate(any())).thenReturn(true);

        TransferRequestController controller = new TransferRequestController(transferRequestService,
            validUserService(), bedService, roomService, mock(BuildingService.class), historyService,
            mock(DormManagerScopeService.class));

        Result<Boolean> result = controller.save(approvedRequest(7L, 1L, 10L));

        org.assertj.core.api.Assertions.assertThat(result.getCode()).isEqualTo(200);
        ArgumentCaptor<Wrapper<Room>> lockCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(roomService).list(lockCaptor.capture());
        org.assertj.core.api.Assertions.assertThat(lockCaptor.getValue().getSqlSegment())
            .contains("ORDER BY id ASC")
            .contains("FOR UPDATE");
        org.assertj.core.api.Assertions.assertThat(
            ((com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Room>) lockCaptor.getValue())
                .getParamNameValuePairs().values()).contains(10L, 20L);
        InOrder writes = inOrder(roomService, bedService, historyService);
        writes.verify(roomService).list(org.mockito.ArgumentMatchers.<Wrapper<Room>>any());
        writes.verify(bedService, atLeastOnce()).update(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any());
        writes.verify(historyService).save(any(StayHistory.class));
        ArgumentCaptor<Wrapper<Bed>> bedQueryCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(bedService, atLeastOnce()).list(bedQueryCaptor.capture());
        Wrapper<Bed> sourceRoomRefresh = bedQueryCaptor.getAllValues().stream()
            .filter(query -> bedQueryValues(query).contains(20L))
            .findFirst()
            .orElseThrow();
        org.assertj.core.api.Assertions.assertThat(sourceRoomRefresh.getSqlSegment()).contains("FOR UPDATE");
    }

    @Test
    void approveLocksStudentBeforeReadingCurrentAssignment() {
        MockHttpServletRequest requestContext = new MockHttpServletRequest();
        requestContext.setAttribute("currentUserId", 99L);
        requestContext.setAttribute("currentUserRole", "dormmanager");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(requestContext));
        TransferRequestService transferRequestService = mock(TransferRequestService.class);
        UserService userService = mock(UserService.class);
        BedService bedService = mock(BedService.class);
        RoomService roomService = mock(RoomService.class);
        StayHistoryService historyService = mock(StayHistoryService.class);
        Bed currentBed = bed(1L, 20L, 7L, "OCCUPIED");
        Bed targetBed = bed(2L, 10L, null, "EMPTY");
        Room targetRoom = room(10L, 1, "NORMAL");
        Room currentRoom = room(20L, 1, "FULL");
        when(userService.getOne(any(Wrapper.class))).thenReturn(new com.dorm.backend.entity.User());
        TransferRequest existingRequest = approvedRequest(7L, 1L, 10L);
        existingRequest.setStatus("PENDING");
        when(transferRequestService.getOne(any(Wrapper.class))).thenReturn(existingRequest);
        when(bedService.getById(1L)).thenReturn(currentBed);
        when(bedService.getOne(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any())).thenReturn(currentBed);
        when(bedService.list(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any()))
            .thenAnswer(invocation -> bedsForRoom(invocation.getArgument(0), currentBed, targetBed));
        stubLockedRooms(roomService, targetRoom, currentRoom);
        when(bedService.update(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any())).thenReturn(true);
        when(historyService.save(any(StayHistory.class))).thenReturn(true);
        when(roomService.updateById(any(Room.class))).thenReturn(true);
        when(transferRequestService.saveOrUpdate(any())).thenReturn(true);
        DormManagerScopeService scopeService = mock(DormManagerScopeService.class);
        when(scopeService.canManageRoom(99L, 20L)).thenReturn(true);
        when(scopeService.canManageRoom(99L, 10L)).thenReturn(true);

        TransferRequestController controller = new TransferRequestController(transferRequestService,
            userService, bedService, roomService, mock(BuildingService.class), historyService,
            scopeService);
        controller.save(approvedRequest(7L, 1L, 10L));

        ArgumentCaptor<Wrapper<com.dorm.backend.entity.User>> userLockCaptor =
            ArgumentCaptor.forClass(Wrapper.class);
        verify(userService).getOne(userLockCaptor.capture());
        org.assertj.core.api.Assertions.assertThat(userLockCaptor.getValue().getSqlSegment())
            .contains("FOR UPDATE");
        org.assertj.core.api.Assertions.assertThat(
            ((com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.dorm.backend.entity.User>)
                userLockCaptor.getValue()).getParamNameValuePairs().values()).contains(7L);
        ArgumentCaptor<Wrapper<TransferRequest>> requestLockCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(transferRequestService).getOne(requestLockCaptor.capture());
        org.assertj.core.api.Assertions.assertThat(requestLockCaptor.getValue().getSqlSegment())
            .contains("FOR UPDATE");
        InOrder order = inOrder(transferRequestService, userService, bedService, roomService);
        order.verify(transferRequestService).getOne(any(Wrapper.class));
        order.verify(userService).getOne(any(Wrapper.class));
        order.verify(bedService, times(2)).getById(1L);
        order.verify(roomService).list(any(Wrapper.class));
        order.verify(bedService, atLeastOnce()).getOne(any(Wrapper.class));
        ArgumentCaptor<Wrapper<Bed>> currentBedCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(bedService, atLeastOnce()).getOne(currentBedCaptor.capture());
        org.assertj.core.api.Assertions.assertThat(currentBedCaptor.getAllValues())
            .allSatisfy(query -> org.assertj.core.api.Assertions.assertThat(query.getSqlSegment())
                .contains("FOR UPDATE"));
    }

    @Test
    void adminApprovalLocksExistingRequestBeforeStudent() {
        MockHttpServletRequest requestContext = new MockHttpServletRequest();
        requestContext.setAttribute("currentUserId", 1L);
        requestContext.setAttribute("currentUserRole", "admin");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(requestContext));
        TransferRequestService transferRequestService = mock(TransferRequestService.class);
        UserService userService = mock(UserService.class);
        TransferRequest existingRequest = approvedRequest(7L, 1L, 10L);
        existingRequest.setStatus("PENDING");
        when(transferRequestService.getOne(any(Wrapper.class))).thenReturn(existingRequest);

        TransferRequestController controller = new TransferRequestController(transferRequestService,
            userService, mock(BedService.class), mock(RoomService.class), mock(BuildingService.class),
            mock(StayHistoryService.class), mock(DormManagerScopeService.class));

        controller.save(approvedRequest(7L, 1L, 10L));

        InOrder order = inOrder(transferRequestService, userService);
        order.verify(transferRequestService).getOne(any(Wrapper.class));
        order.verify(userService).getOne(any(Wrapper.class));
    }

    @Test
    void approveRejectsLegacyMultipleBedAssignments() {
        TransferRequestService transferRequestService = mock(TransferRequestService.class);
        BedService bedService = mock(BedService.class);
        RoomService roomService = mock(RoomService.class);
        Bed firstBed = bed(1L, 20L, 7L, "OCCUPIED");
        Bed duplicateBed = bed(2L, 30L, 7L, "OCCUPIED");
        when(bedService.getById(1L)).thenReturn(firstBed);
        when(bedService.getOne(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any())).thenReturn(firstBed);
        when(bedService.list(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any()))
            .thenReturn(List.of(firstBed, duplicateBed));
        stubLockedRooms(roomService, room(10L, 1, "NORMAL"), room(20L, 1, "FULL"));

        TransferRequestController controller = new TransferRequestController(transferRequestService,
            validUserService(), bedService, roomService, mock(BuildingService.class),
            mock(StayHistoryService.class), mock(DormManagerScopeService.class));

        Result<Boolean> result = controller.save(approvedRequest(7L, 1L, 10L));

        org.assertj.core.api.Assertions.assertThat(result.getCode()).isEqualTo(409);
        org.assertj.core.api.Assertions.assertThat(result.getMessage())
            .isEqualTo("该学生存在多个床位分配，请先清理异常数据");
        verify(bedService, never()).update(any(Wrapper.class));
        verify(transferRequestService, never()).saveOrUpdate(any(TransferRequest.class));
    }

    @Test
    void approveRejectsMissingStudentBeforeReadingBedState() {
        TransferRequestService transferRequestService = mock(TransferRequestService.class);
        UserService userService = mock(UserService.class);
        BedService bedService = mock(BedService.class);
        TransferRequestController controller = new TransferRequestController(transferRequestService,
            userService, bedService, mock(RoomService.class), mock(BuildingService.class),
            mock(StayHistoryService.class), mock(DormManagerScopeService.class));

        Result<Boolean> result = controller.save(approvedRequest(7L, 1L, 10L));

        org.assertj.core.api.Assertions.assertThat(result.getCode()).isEqualTo(400);
        org.assertj.core.api.Assertions.assertThat(result.getMessage()).isEqualTo("申请学生不存在");
        verify(bedService, never()).getOne(any(Wrapper.class));
        verify(bedService, never()).list(any(Wrapper.class));
    }

    @Test
    void approveThrowsConflictWhenTargetBedWasClaimedConcurrently() {
        TransferRequestService transferRequestService = mock(TransferRequestService.class);
        BedService bedService = mock(BedService.class);
        RoomService roomService = mock(RoomService.class);
        StayHistoryService historyService = mock(StayHistoryService.class);
        Bed currentBed = bed(1L, 1L, 7L, "OCCUPIED");
        Bed targetBed = bed(2L, 2L, null, "EMPTY");
        when(bedService.getById(1L)).thenReturn(currentBed);
        when(bedService.getOne(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any())).thenReturn(currentBed);
        when(bedService.list(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any()))
            .thenAnswer(invocation -> bedsForRoom(invocation.getArgument(0), currentBed, targetBed));
        when(roomService.getById(2L)).thenReturn(room(2L, 1, "NORMAL"));
        stubLockedRooms(roomService, room(1L, 1, "FULL"), room(2L, 1, "NORMAL"));
        when(bedService.update(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any()))
            .thenReturn(true, false);

        TransferRequestController controller = new TransferRequestController(transferRequestService,
            validUserService(), bedService, roomService, mock(BuildingService.class), historyService,
            mock(DormManagerScopeService.class));

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> controller.save(approvedRequest(7L, 1L, 2L)))
            .isInstanceOf(BedAllocationConflictException.class)
            .hasMessage("目标床位已被占用，请刷新后重试");
        verify(historyService, never()).save(any(StayHistory.class));
        verify(transferRequestService, never()).saveOrUpdate(any(TransferRequest.class));
    }

    private Bed bed(Long id, Long roomId, Long studentId, String status) {
        Bed bed = new Bed();
        bed.setId(id);
        bed.setRoomId(roomId);
        bed.setStudentId(studentId);
        bed.setStatus(status);
        return bed;
    }

    private Room room(Long id, Integer capacity, String status) {
        Room room = new Room();
        room.setId(id);
        room.setCapacity(capacity);
        room.setStatus(status);
        return room;
    }

    private TransferRequest approvedRequest(Long studentId, Long currentBedId, Long targetRoomId) {
        TransferRequest request = new TransferRequest();
        request.setId(10L);
        request.setStudentId(studentId);
        request.setCurrentBedId(currentBedId);
        request.setTargetRoomId(targetRoomId);
        request.setStatus("APPROVED");
        return request;
    }

    private void stubLockedRooms(RoomService roomService, Room... rooms) {
        when(roomService.list(org.mockito.ArgumentMatchers.<Wrapper<Room>>any()))
            .thenReturn(List.of(rooms));
    }

    private UserService validUserService() {
        UserService userService = mock(UserService.class);
        when(userService.getOne(any(Wrapper.class))).thenReturn(new com.dorm.backend.entity.User());
        return userService;
    }

    private List<Bed> bedsForRoom(Wrapper<Bed> wrapper, Bed currentBed, Bed targetBed) {
        wrapper.getSqlSegment();
        java.util.Collection<Object> values = bedQueryValues(wrapper);
        if (values.contains(targetBed.getRoomId())) return List.of(targetBed);
        if (values.contains(currentBed.getRoomId())) return List.of(currentBed);
        return List.of();
    }

    private java.util.Collection<Object> bedQueryValues(Wrapper<Bed> wrapper) {
        wrapper.getSqlSegment();
        return ((com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Bed>) wrapper)
            .getParamNameValuePairs().values();
    }
}
