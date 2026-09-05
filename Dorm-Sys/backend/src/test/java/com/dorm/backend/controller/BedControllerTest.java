package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.common.BedAllocationConflictException;
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
import org.mockito.InOrder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.inOrder;
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
    void deleteRunsInsideTransaction() throws Exception {
        assertThat(BedController.class.getMethod("delete", Long.class)
                .isAnnotationPresent(Transactional.class))
            .isTrue();
    }

    @Test
    void deleteRejectsOccupiedBed() {
        BedService bedService = mock(BedService.class);
        StayHistoryService historyService = mock(StayHistoryService.class);
        RoomService roomService = mock(RoomService.class);
        Bed initial = bed(1L, 10L, null, "EMPTY");
        Bed locked = bed(1L, 10L, 7L, "OCCUPIED");
        when(bedService.getById(1L)).thenReturn(initial);
        when(roomService.getOne(org.mockito.ArgumentMatchers.<Wrapper<Room>>any()))
            .thenReturn(room(10L, 1, "NORMAL"));
        when(bedService.list(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any())).thenReturn(List.of(locked));

        Result<Boolean> result = controller(bedService, historyService, roomService).delete(1L);

        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).isEqualTo("已入住的床位不能删除");
        verify(bedService, never()).remove(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any());
    }

    @Test
    void deleteRejectsBedReferencedByStayHistory() {
        BedService bedService = mock(BedService.class);
        StayHistoryService historyService = mock(StayHistoryService.class);
        RoomService roomService = mock(RoomService.class);
        Bed bed = bed(1L, 10L, null, "EMPTY");
        when(bedService.getById(1L)).thenReturn(bed);
        when(roomService.getOne(org.mockito.ArgumentMatchers.<Wrapper<Room>>any()))
            .thenReturn(room(10L, 2, "NORMAL"));
        when(bedService.list(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any()))
            .thenReturn(List.of(bed, bed(2L, 10L, null, "EMPTY")));
        when(historyService.list(org.mockito.ArgumentMatchers.<Wrapper<StayHistory>>any()))
            .thenReturn(List.of(new StayHistory()));

        Result<Boolean> result = controller(bedService, historyService, roomService).delete(1L);

        assertThat(result.getCode()).isEqualTo(400);
        verify(bedService, never()).remove(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any());
    }

    @Test
    void deleteSynchronizesRoomCapacityAndStatus() {
        BedService bedService = mock(BedService.class);
        StayHistoryService historyService = mock(StayHistoryService.class);
        RoomService roomService = mock(RoomService.class);
        Bed deletedBed = bed(1L, 10L, null, "EMPTY");
        Bed remainingBed = bed(2L, 10L, 7L, "OCCUPIED");
        Room room = room(10L, 2, "NORMAL");
        when(bedService.getById(1L)).thenReturn(deletedBed);
        when(roomService.getOne(org.mockito.ArgumentMatchers.<Wrapper<Room>>any())).thenReturn(room);
        when(bedService.list(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any()))
            .thenReturn(List.of(deletedBed, remainingBed));
        when(historyService.list(org.mockito.ArgumentMatchers.<Wrapper<StayHistory>>any())).thenReturn(List.of());
        when(bedService.remove(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any())).thenReturn(true);
        when(roomService.updateById(room)).thenReturn(true);

        Result<Boolean> result = controller(bedService, historyService, roomService).delete(1L);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(room.getCapacity()).isEqualTo(1);
        assertThat(room.getStatus()).isEqualTo("FULL");
        ArgumentCaptor<Wrapper<Room>> roomLockCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(roomService).getOne(roomLockCaptor.capture());
        assertThat(roomLockCaptor.getValue().getSqlSegment()).contains("FOR UPDATE");
        ArgumentCaptor<Wrapper<Bed>> bedLockCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(bedService).list(bedLockCaptor.capture());
        assertThat(bedLockCaptor.getValue().getSqlSegment()).contains("FOR UPDATE");
        verify(roomService).updateById(room);
    }

    @Test
    void deleteFailsAtomicallyWhenBedRemovalFails() {
        BedService bedService = mock(BedService.class);
        StayHistoryService historyService = mock(StayHistoryService.class);
        RoomService roomService = mock(RoomService.class);
        Bed deletedBed = bed(1L, 10L, null, "EMPTY");
        when(bedService.getById(1L)).thenReturn(deletedBed);
        when(roomService.getOne(org.mockito.ArgumentMatchers.<Wrapper<Room>>any()))
            .thenReturn(room(10L, 2, "NORMAL"));
        when(bedService.list(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any()))
            .thenReturn(List.of(deletedBed, bed(2L, 10L, null, "EMPTY")));
        when(historyService.list(org.mockito.ArgumentMatchers.<Wrapper<StayHistory>>any())).thenReturn(List.of());
        when(bedService.remove(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any())).thenReturn(false);

        assertThatThrownBy(() -> controller(bedService, historyService, roomService).delete(1L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("删除床位失败");
        verify(roomService, never()).updateById(any(Room.class));
    }

    @Test
    void deleteRejectsLastBedInRoom() {
        BedService bedService = mock(BedService.class);
        StayHistoryService historyService = mock(StayHistoryService.class);
        RoomService roomService = mock(RoomService.class);
        Bed deletedBed = bed(1L, 10L, null, "EMPTY");
        when(bedService.getById(1L)).thenReturn(deletedBed);
        when(roomService.getOne(org.mockito.ArgumentMatchers.<Wrapper<Room>>any()))
            .thenReturn(room(10L, 1, "NORMAL"));
        when(bedService.list(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any()))
            .thenReturn(List.of(deletedBed));
        when(historyService.list(org.mockito.ArgumentMatchers.<Wrapper<StayHistory>>any())).thenReturn(List.of());

        Result<Boolean> result = controller(bedService, historyService, roomService).delete(1L);

        assertThat(result.getCode()).isEqualTo(400);
        verify(bedService, never()).remove(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any());
    }

    @Test
    void createBedDirectlyIsRejected() {
        BedService bedService = mock(BedService.class);
        Bed submitted = bed(null, 10L, null, "EMPTY");

        Result<Boolean> result = controller(bedService, mock(StayHistoryService.class),
            mock(RoomService.class)).save(submitted);

        assertThat(result.getCode()).isEqualTo(400);
        verify(bedService, never()).save(any(Bed.class));
    }

    @Test
    void saveRejectsChangingExistingBedRoom() {
        BedService bedService = mock(BedService.class);
        StayHistoryService historyService = mock(StayHistoryService.class);
        RoomService roomService = mock(RoomService.class);
        Bed existing = bed(1L, 10L, null, "EMPTY");
        Bed submitted = bed(1L, 20L, null, "EMPTY");
        when(bedService.getById(1L)).thenReturn(existing);
        when(bedService.update(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any())).thenReturn(true);

        Result<Boolean> result = controller(bedService, historyService, roomService).save(submitted);

        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).isEqualTo("不能通过床位管理调整所属房间");
        verify(bedService, never()).update(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any());
        verify(historyService, never()).save(any(StayHistory.class));
    }

    @Test
    void checkInUsesExistingBedStateAsCompareAndSet() {
        BedService bedService = mock(BedService.class);
        StayHistoryService historyService = mock(StayHistoryService.class);
        RoomService roomService = mock(RoomService.class);
        Bed existing = bed(1L, 10L, null, "EMPTY");
        Bed submitted = bed(1L, 10L, 7L, null);
        when(bedService.getById(1L)).thenReturn(existing);
        when(bedService.list(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any())).thenReturn(List.of());
        when(bedService.update(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any())).thenReturn(true);
        when(historyService.save(any(StayHistory.class))).thenReturn(true);
        stubLockedRooms(roomService, room(10L, 1, "NORMAL"));
        when(roomService.getById(10L)).thenReturn(room(10L, 1, "NORMAL"));

        controller(bedService, historyService, roomService).save(submitted);

        ArgumentCaptor<Wrapper<Bed>> updateCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(bedService).update(updateCaptor.capture());
        UpdateWrapper<Bed> update = (UpdateWrapper<Bed>) updateCaptor.getValue();
        String sql = update.getSqlSegment();
        assertThat(sql).contains("student_id IS NULL").contains("status =");
        assertThat(update.getParamNameValuePairs().values()).contains(1L, 10L, "EMPTY");
    }

    @Test
    void checkInLocksStudentBeforeReadingExistingAssignments() {
        BedService bedService = mock(BedService.class);
        UserService userService = mock(UserService.class);
        StayHistoryService historyService = mock(StayHistoryService.class);
        RoomService roomService = mock(RoomService.class);
        Bed existing = bed(1L, 10L, null, "EMPTY");
        Bed submitted = bed(1L, 10L, 7L, null);
        when(bedService.getById(1L)).thenReturn(existing);
        when(userService.getOne(any(Wrapper.class))).thenReturn(new com.dorm.backend.entity.User());
        when(bedService.list(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any())).thenReturn(List.of());
        when(bedService.update(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any())).thenReturn(true);
        when(historyService.save(any(StayHistory.class))).thenReturn(true);
        stubLockedRooms(roomService, room(10L, 1, "NORMAL"));

        BedController controller = new BedController(bedService, userService, historyService,
            roomService, mock(DormManagerScopeService.class));
        controller.save(submitted);

        ArgumentCaptor<Wrapper<com.dorm.backend.entity.User>> userLockCaptor =
            ArgumentCaptor.forClass(Wrapper.class);
        verify(userService).getOne(userLockCaptor.capture());
        assertThat(userLockCaptor.getValue().getSqlSegment()).contains("FOR UPDATE");
        assertThat(((com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.dorm.backend.entity.User>)
            userLockCaptor.getValue()).getParamNameValuePairs().values()).contains(7L);
        InOrder order = inOrder(userService, bedService);
        order.verify(userService).getOne(any(Wrapper.class));
        order.verify(bedService).getById(1L);
    }

    @Test
    void checkInRejectsMissingStudentBeforeReadingBedState() {
        BedService bedService = mock(BedService.class);
        UserService userService = mock(UserService.class);
        Bed submitted = bed(1L, 10L, 7L, null);
        BedController controller = new BedController(bedService, userService,
            mock(StayHistoryService.class), mock(RoomService.class),
            mock(DormManagerScopeService.class));

        Result<Boolean> result = controller.save(submitted);

        assertThat(result.getCode()).isEqualTo(400);
        assertThat(result.getMessage()).isEqualTo("入住学生不存在");
        verify(bedService, never()).getById(any());
    }

    @Test
    void checkInThrowsConflictWhenBedChangedAfterItWasRead() {
        BedService bedService = mock(BedService.class);
        StayHistoryService historyService = mock(StayHistoryService.class);
        RoomService roomService = mock(RoomService.class);
        Bed existing = bed(1L, 10L, null, "EMPTY");
        Bed submitted = bed(1L, 10L, 7L, null);
        when(bedService.getById(1L)).thenReturn(existing);
        when(bedService.list(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any())).thenReturn(List.of());
        when(bedService.update(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any())).thenReturn(false);
        when(historyService.save(any(StayHistory.class))).thenReturn(true);
        stubLockedRooms(roomService, room(10L, 1, "NORMAL"));
        when(roomService.getById(10L)).thenReturn(room(10L, 1, "NORMAL"));

        assertThatThrownBy(() -> controller(bedService, historyService, roomService).save(submitted))
            .isInstanceOf(BedAllocationConflictException.class)
            .hasMessage("床位状态已变化，请刷新后重试");
        verify(roomService, never()).updateById(any(Room.class));
    }

    @Test
    void checkInStopsWhenPreviouslyReadBedChanged() {
        BedService bedService = mock(BedService.class);
        StayHistoryService historyService = mock(StayHistoryService.class);
        RoomService roomService = mock(RoomService.class);
        Bed previousBed = bed(1L, 10L, 7L, "OCCUPIED");
        Bed targetBed = bed(2L, 20L, null, "EMPTY");
        Bed submitted = bed(2L, 20L, 7L, null);
        when(bedService.getById(2L)).thenReturn(targetBed);
        when(bedService.list(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any())).thenReturn(List.of(previousBed));
        when(bedService.update(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any())).thenReturn(false);
        stubLockedRooms(roomService, room(10L, 1, "FULL"), room(20L, 1, "NORMAL"));
        when(roomService.getById(20L)).thenReturn(room(20L, 1, "NORMAL"));

        assertThatThrownBy(() -> controller(bedService, historyService, roomService).save(submitted))
            .isInstanceOf(BedAllocationConflictException.class)
            .hasMessage("原床位状态已变化，请刷新后重试");
        verify(historyService, never()).getOne(org.mockito.ArgumentMatchers.<Wrapper<StayHistory>>any());
        verify(historyService, never()).save(any(StayHistory.class));
        verify(bedService).update(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any());
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
        stubLockedRooms(roomService, previousRoom, targetRoom);
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
        ArgumentCaptor<Wrapper<Bed>> updateCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(bedService, org.mockito.Mockito.times(2)).update(updateCaptor.capture());
        UpdateWrapper<Bed> previousRelease = (UpdateWrapper<Bed>) updateCaptor.getAllValues().get(0);
        previousRelease.getSqlSegment();
        assertThat(previousRelease.getParamNameValuePairs().values())
            .contains(1L, 10L, 7L, "OCCUPIED");
        UpdateWrapper<Bed> targetClaim = (UpdateWrapper<Bed>) updateCaptor.getAllValues().get(1);
        targetClaim.getSqlSegment();
        assertThat(targetClaim.getParamNameValuePairs().values())
            .contains(2L, 20L, "EMPTY");
    }

    @Test
    void checkInLocksAllAffectedRoomsInIdOrderBeforeWrites() {
        BedService bedService = mock(BedService.class);
        StayHistoryService historyService = mock(StayHistoryService.class);
        RoomService roomService = mock(RoomService.class);
        Bed previousBed = bed(1L, 20L, 7L, "OCCUPIED");
        Bed targetBed = bed(2L, 10L, null, "EMPTY");
        Bed submitted = bed(2L, 10L, 7L, null);
        Room targetRoom = room(10L, 1, "NORMAL");
        Room previousRoom = room(20L, 1, "FULL");

        when(bedService.getById(2L)).thenReturn(targetBed);
        when(bedService.list(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any())).thenAnswer(invocation -> {
            Wrapper<Bed> query = invocation.getArgument(0);
            if (queryValues(query).contains(7L)) return List.of(previousBed);
            if (queryValues(query).contains(10L)) return List.of(submitted);
            if (queryValues(query).contains(20L)) return List.of();
            return List.of();
        });
        when(roomService.list(org.mockito.ArgumentMatchers.<Wrapper<Room>>any()))
            .thenReturn(List.of(targetRoom, previousRoom));
        when(roomService.getById(10L)).thenReturn(targetRoom);
        when(roomService.getById(20L)).thenReturn(previousRoom);
        when(bedService.update(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any())).thenReturn(true);
        when(historyService.save(any(StayHistory.class))).thenReturn(true);
        when(roomService.updateById(any(Room.class))).thenReturn(true);

        Result<Boolean> result = controller(bedService, historyService, roomService).save(submitted);

        assertThat(result.getCode()).isEqualTo(200);
        ArgumentCaptor<Wrapper<Room>> lockCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(roomService).list(lockCaptor.capture());
        assertThat(lockCaptor.getValue().getSqlSegment())
            .contains("ORDER BY id ASC")
            .contains("FOR UPDATE");
        assertThat(((com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Room>) lockCaptor.getValue())
            .getParamNameValuePairs().values()).contains(10L, 20L);
        InOrder bedWrites = inOrder(roomService, bedService);
        bedWrites.verify(roomService).list(org.mockito.ArgumentMatchers.<Wrapper<Room>>any());
        bedWrites.verify(bedService, atLeastOnce()).update(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any());
        InOrder historyWrites = inOrder(roomService, historyService);
        historyWrites.verify(roomService).list(org.mockito.ArgumentMatchers.<Wrapper<Room>>any());
        historyWrites.verify(historyService).save(any(StayHistory.class));
        ArgumentCaptor<Wrapper<Bed>> bedQueryCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(bedService, atLeastOnce()).list(bedQueryCaptor.capture());
        Wrapper<Bed> previousRoomRefresh = bedQueryCaptor.getAllValues().stream()
            .filter(query -> queryValues(query).contains(20L))
            .findFirst()
            .orElseThrow();
        assertThat(previousRoomRefresh.getSqlSegment()).contains("FOR UPDATE");
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
        stubLockedRooms(roomService, room);
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
        stubLockedRooms(roomService, room(10L, 1, "FULL"));

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
        stubLockedRooms(roomService, room);
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
        stubLockedRooms(roomService, room(10L, 1, "MAINTENANCE"));
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
        stubLockedRooms(roomService, room(10L, 1, "NORMAL"));
        when(roomService.getById(10L)).thenReturn(room(10L, 1, "NORMAL"));

        Result<Boolean> result = controller(bedService, historyService, roomService).save(submitted);

        assertThat(result.getCode()).isEqualTo(400);
        verify(bedService, never()).update(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any());
        verify(historyService, never()).save(any(StayHistory.class));
    }

    private BedController controller(BedService bedService, StayHistoryService historyService,
                                     RoomService roomService) {
        UserService userService = mock(UserService.class);
        when(userService.getOne(any(Wrapper.class))).thenReturn(new com.dorm.backend.entity.User());
        return new BedController(bedService, userService, historyService,
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

    private void stubLockedRooms(RoomService roomService, Room... rooms) {
        when(roomService.list(org.mockito.ArgumentMatchers.<Wrapper<Room>>any()))
            .thenReturn(List.of(rooms));
    }

    private java.util.Collection<Object> queryValues(Wrapper<Bed> wrapper) {
        wrapper.getSqlSegment();
        return ((com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Bed>) wrapper)
            .getParamNameValuePairs().values();
    }
}
