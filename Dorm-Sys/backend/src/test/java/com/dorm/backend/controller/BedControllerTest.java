package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.entity.Bed;
import com.dorm.backend.entity.Room;
import com.dorm.backend.entity.StayHistory;
import com.dorm.backend.service.BedService;
import com.dorm.backend.service.DormManagerScopeService;
import com.dorm.backend.service.RoomService;
import com.dorm.backend.service.StayHistoryService;
import com.dorm.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BedControllerTest {

    @Test
    void saveRunsInsideTransaction() throws Exception {
        assertThat(BedController.class.getMethod("save", Bed.class)
                .isAnnotationPresent(Transactional.class))
            .isTrue();
    }

    @Test
    void checkInMovesStudentHistoryAndRefreshesBothRooms() {
        BedService bedService = mock(BedService.class);
        StayHistoryService historyService = mock(StayHistoryService.class);
        RoomService roomService = mock(RoomService.class);

        Bed previousBed = bed(1L, 10L, 7L, "OCCUPIED");
        Bed targetBed = bed(2L, 20L, null, "EMPTY");
        Bed submitted = bed(2L, 20L, 7L, null);
        StayHistory previousHistory = new StayHistory();
        previousHistory.setId(100L);
        previousHistory.setStudentId(7L);
        previousHistory.setBedId(1L);
        Room previousRoom = room(10L, 1, "FULL");
        Room targetRoom = room(20L, 1, "NORMAL");

        when(bedService.getById(2L)).thenReturn(targetBed);
        when(bedService.list(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any())).thenAnswer(invocation -> {
            Wrapper<Bed> query = invocation.getArgument(0);
            if (queryValues(query).contains(7L)) return List.of(previousBed);
            if (queryValues(query).contains(10L)) return List.of();
            if (queryValues(query).contains(20L)) return List.of(submitted);
            return List.of();
        });
        when(bedService.update(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any())).thenReturn(true);
        when(historyService.getOne(org.mockito.ArgumentMatchers.<Wrapper<StayHistory>>any())).thenReturn(previousHistory);
        when(historyService.updateById(previousHistory)).thenReturn(true);
        when(historyService.save(any(StayHistory.class))).thenReturn(true);
        when(roomService.getById(10L)).thenReturn(previousRoom);
        when(roomService.getById(20L)).thenReturn(targetRoom);
        when(roomService.updateById(any(Room.class))).thenReturn(true);

        BedController controller = controller(bedService, historyService, roomService);

        Result<Boolean> result = controller.save(submitted);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(previousHistory.getCheckOutDate()).isNotNull();
        assertThat(submitted.getStatus()).isEqualTo("OCCUPIED");
        assertThat(previousRoom.getStatus()).isEqualTo("NORMAL");
        assertThat(targetRoom.getStatus()).isEqualTo("FULL");
        ArgumentCaptor<StayHistory> historyCaptor = ArgumentCaptor.forClass(StayHistory.class);
        verify(historyService).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getStudentId()).isEqualTo(7L);
        assertThat(historyCaptor.getValue().getBedId()).isEqualTo(2L);
        assertThat(historyCaptor.getValue().getCheckInDate()).isNotNull();
    }

    @Test
    void checkOutClosesHistoryAndMarksRoomAvailable() {
        BedService bedService = mock(BedService.class);
        StayHistoryService historyService = mock(StayHistoryService.class);
        RoomService roomService = mock(RoomService.class);

        Bed existing = bed(1L, 10L, 7L, "OCCUPIED");
        Bed submitted = bed(1L, null, null, null);
        StayHistory history = new StayHistory();
        history.setId(100L);
        history.setStudentId(7L);
        history.setBedId(1L);
        Room room = room(10L, 1, "FULL");

        when(bedService.getById(1L)).thenReturn(existing);
        when(bedService.update(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any())).thenReturn(true);
        when(bedService.list(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any())).thenReturn(List.of());
        when(historyService.getOne(org.mockito.ArgumentMatchers.<Wrapper<StayHistory>>any())).thenReturn(history);
        when(historyService.updateById(history)).thenReturn(true);
        when(roomService.getById(10L)).thenReturn(room);
        when(roomService.updateById(room)).thenReturn(true);

        Result<Boolean> result = controller(bedService, historyService, roomService).save(submitted);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(history.getCheckOutDate()).isNotNull();
        assertThat(submitted.getStatus()).isEqualTo("EMPTY");
        assertThat(room.getStatus()).isEqualTo("NORMAL");
    }

    @Test
    void checkOutStopsBeforeUpdatingBedWhenHistoryCannotBeClosed() {
        BedService bedService = mock(BedService.class);
        StayHistoryService historyService = mock(StayHistoryService.class);
        RoomService roomService = mock(RoomService.class);
        Bed existing = bed(1L, 10L, 7L, "OCCUPIED");
        StayHistory history = new StayHistory();
        when(bedService.getById(1L)).thenReturn(existing);
        when(historyService.getOne(org.mockito.ArgumentMatchers.<Wrapper<StayHistory>>any())).thenReturn(history);
        when(historyService.updateById(history)).thenReturn(false);

        Bed submitted = bed(1L, null, null, null);

        assertThatThrownBy(() -> controller(bedService, historyService, roomService).save(submitted))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("关闭住宿记录失败");
        verify(bedService, never()).update(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any());
    }

    @Test
    void checkOutDoesNotClearMaintenanceRoomStatus() {
        BedService bedService = mock(BedService.class);
        StayHistoryService historyService = mock(StayHistoryService.class);
        RoomService roomService = mock(RoomService.class);

        Bed existing = bed(1L, 10L, 7L, "OCCUPIED");
        Bed submitted = bed(1L, null, null, null);
        StayHistory history = new StayHistory();
        Room room = room(10L, 1, "MAINTENANCE");
        when(bedService.getById(1L)).thenReturn(existing);
        when(bedService.update(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any())).thenReturn(true);
        when(historyService.getOne(org.mockito.ArgumentMatchers.<Wrapper<StayHistory>>any())).thenReturn(history);
        when(historyService.updateById(history)).thenReturn(true);
        when(roomService.getById(10L)).thenReturn(room);

        Result<Boolean> result = controller(bedService, historyService, roomService).save(submitted);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(room.getStatus()).isEqualTo("MAINTENANCE");
        verify(roomService, never()).updateById(room);
    }

    @Test
    void checkInRejectsMaintenanceRoom() {
        BedService bedService = mock(BedService.class);
        StayHistoryService historyService = mock(StayHistoryService.class);
        RoomService roomService = mock(RoomService.class);

        Bed existing = bed(1L, 10L, null, "EMPTY");
        Bed submitted = bed(1L, null, 7L, null);
        when(bedService.getById(1L)).thenReturn(existing);
        when(bedService.update(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any())).thenReturn(true);
        when(historyService.save(any(StayHistory.class))).thenReturn(true);
        when(roomService.getById(10L)).thenReturn(room(10L, 1, "MAINTENANCE"));

        Result<Boolean> result = controller(bedService, historyService, roomService).save(submitted);

        assertThat(result.getCode()).isEqualTo(400);
        verify(bedService, never()).update(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any());
        verify(historyService, never()).save(any(StayHistory.class));
    }

    @Test
    void checkInRejectsBrokenBed() {
        BedService bedService = mock(BedService.class);
        StayHistoryService historyService = mock(StayHistoryService.class);
        RoomService roomService = mock(RoomService.class);

        Bed existing = bed(1L, 10L, null, "BROKEN");
        Bed submitted = bed(1L, null, 7L, null);
        when(bedService.getById(1L)).thenReturn(existing);
        when(bedService.update(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any())).thenReturn(true);
        when(historyService.save(any(StayHistory.class))).thenReturn(true);
        when(roomService.getById(10L)).thenReturn(room(10L, 1, "NORMAL"));

        Result<Boolean> result = controller(bedService, historyService, roomService).save(submitted);

        assertThat(result.getCode()).isEqualTo(400);
        verify(bedService, never()).update(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any());
        verify(historyService, never()).save(any(StayHistory.class));
    }

    private BedController controller(BedService bedService, StayHistoryService historyService,
                                     RoomService roomService) {
        return new BedController(bedService, mock(UserService.class), historyService,
            roomService, mock(DormManagerScopeService.class));
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

    private java.util.Collection<Object> queryValues(Wrapper<Bed> wrapper) {
        wrapper.getSqlSegment();
        return ((com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Bed>) wrapper)
            .getParamNameValuePairs().values();
    }
}
