package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.entity.Bed;
import com.dorm.backend.entity.Room;
import com.dorm.backend.entity.StayHistory;
import com.dorm.backend.entity.TransferRequest;
import com.dorm.backend.service.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransferRequestControllerTest {

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
        when(bedService.list(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any()))
            .thenAnswer(invocation -> bedsForRoom(invocation.getArgument(0), currentBed, targetBed));
        when(roomService.getById(org.mockito.ArgumentMatchers.anyLong())).thenReturn(maintenanceRoom);
        when(transferRequestService.saveOrUpdate(any())).thenReturn(true);

        TransferRequestController controller = new TransferRequestController(transferRequestService, mock(UserService.class),
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
        when(bedService.list(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any()))
            .thenAnswer(invocation -> bedsForRoom(invocation.getArgument(0), currentBed, targetBed));
        when(roomService.getById(1L)).thenReturn(currentRoom);
        when(roomService.getById(2L)).thenReturn(targetRoom);
        when(bedService.update(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any())).thenReturn(true);
        when(bedService.updateById(targetBed)).thenReturn(true);
        when(historyService.save(any(StayHistory.class))).thenReturn(true);
        when(roomService.updateById(any(Room.class))).thenReturn(true);
        when(transferRequestService.saveOrUpdate(any())).thenReturn(true);

        TransferRequestController controller = new TransferRequestController(transferRequestService, mock(UserService.class),
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
        when(bedService.list(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any()))
            .thenAnswer(invocation -> bedsForRoom(invocation.getArgument(0), currentBed, targetBed));
        when(bedService.update(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any())).thenReturn(true);
        when(bedService.updateById(targetBed)).thenReturn(true);
        when(roomService.getById(1L)).thenReturn(currentRoom);
        when(roomService.getById(2L)).thenReturn(targetRoom);
        when(roomService.updateById(any(Room.class))).thenReturn(true);
        when(historyService.getOne(org.mockito.ArgumentMatchers.<Wrapper<StayHistory>>any())).thenReturn(currentHistory);
        when(historyService.updateById(currentHistory)).thenReturn(true);
        when(historyService.save(any(StayHistory.class))).thenReturn(true);
        when(transferRequestService.saveOrUpdate(any())).thenReturn(true);

        TransferRequestController controller = new TransferRequestController(transferRequestService, mock(UserService.class),
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
        verify(bedService, times(2)).update(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any());
        verify(bedService).updateById(targetBed);
        verify(transferRequestService).saveOrUpdate(request);
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

    private List<Bed> bedsForRoom(Wrapper<Bed> wrapper, Bed currentBed, Bed targetBed) {
        wrapper.getSqlSegment();
        java.util.Collection<Object> values =
            ((com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Bed>) wrapper)
                .getParamNameValuePairs().values();
        if (values.contains(targetBed.getRoomId())) return List.of(targetBed);
        if (values.contains(currentBed.getRoomId())) return List.of(currentBed);
        return List.of();
    }
}
