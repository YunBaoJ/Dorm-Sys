package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.dorm.backend.entity.Bed;
import com.dorm.backend.entity.Building;
import com.dorm.backend.entity.Room;
import com.dorm.backend.service.BedService;
import com.dorm.backend.service.BuildingService;
import com.dorm.backend.service.DormManagerScopeService;
import com.dorm.backend.service.RoomService;
import com.dorm.backend.service.StayHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RoomControllerTest {

    private RoomService roomService;
    private BuildingService buildingService;
    private BedService bedService;
    private StayHistoryService stayHistoryService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        roomService = mock(RoomService.class);
        buildingService = mock(BuildingService.class);
        bedService = mock(BedService.class);
        stayHistoryService = mock(StayHistoryService.class);

        RoomController controller = new RoomController(roomService, buildingService, bedService,
            stayHistoryService, mock(DormManagerScopeService.class));
        ReflectionTestUtils.setField(controller, "roomService", roomService);
        ReflectionTestUtils.setField(controller, "buildingService", buildingService);
        ReflectionTestUtils.setField(controller, "bedService", bedService);
        ReflectionTestUtils.setField(controller, "stayHistoryService", stayHistoryService);
        ReflectionTestUtils.setField(controller, "managerScopeService", mock(DormManagerScopeService.class));
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void saveRunsInsideTransaction() throws Exception {
        assertThat(RoomController.class.getMethod("save", Room.class)
                .isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class))
            .isTrue();
    }

    @Test
    void createGeneratesAllBedsAsCheckedBatch() {
        Room room = room(null, 1L, "101", 2);
        when(roomService.saveOrUpdate(room)).thenAnswer(invocation -> {
            room.setId(10L);
            return true;
        });
        AtomicReference<Collection<Bed>> savedBeds = new AtomicReference<>();
        when(bedService.saveBatch(any())).thenAnswer(invocation -> {
            savedBeds.set(invocation.getArgument(0));
            return true;
        });

        RoomController controller = new RoomController(roomService, buildingService, bedService,
            stayHistoryService, mock(DormManagerScopeService.class));
        assertThat(controller.save(room).getCode()).isEqualTo(200);

        assertThat(savedBeds.get()).extracting(Bed::getRoomId).containsOnly(10L);
        assertThat(savedBeds.get()).extracting(Bed::getBedNumber).containsExactly("101-1", "101-2");
    }

    @Test
    void createFailsAtomicallyWhenBedsCannotBeSaved() {
        Room room = room(null, 1L, "101", 2);
        when(roomService.saveOrUpdate(room)).thenAnswer(invocation -> {
            room.setId(10L);
            return true;
        });
        when(bedService.saveBatch(any())).thenReturn(false);

        RoomController controller = new RoomController(roomService, buildingService, bedService,
            stayHistoryService, mock(DormManagerScopeService.class));

        assertThatThrownBy(() -> controller.save(room))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("创建房间床位失败");
    }

    @Test
    void updateRejectsCapacityChangeWhenRoomAlreadyHasBeds() {
        Room existing = room(10L, 1L, "101", 4);
        Room submitted = room(10L, 1L, "101", 6);
        when(roomService.getOne(any(Wrapper.class))).thenReturn(existing);
        when(bedService.list(any(Wrapper.class))).thenReturn(List.of(new Bed()));

        RoomController controller = new RoomController(roomService, buildingService, bedService,
            stayHistoryService, mock(DormManagerScopeService.class));
        com.dorm.backend.common.Result<Boolean> result = controller.save(submitted);

        assertThat(result.getCode()).isEqualTo(400);
        verify(roomService, never()).saveOrUpdate(any(Room.class));
    }

    @Test
    void updateRejectsMissingRoomInsteadOfInsertingIt() {
        Room submitted = room(10L, 1L, "101", 4);
        RoomController controller = new RoomController(roomService, buildingService, bedService,
            stayHistoryService, mock(DormManagerScopeService.class));
        com.dorm.backend.common.Result<Boolean> result = controller.save(submitted);

        assertThat(result.getCode()).isEqualTo(404);
        verify(roomService, never()).saveOrUpdate(any(Room.class));
    }

    @Test
    void updateLocksExistingRoomBeforeCheckingWhetherBedsExist() {
        Room existing = room(10L, 1L, "101", 2);
        Room submitted = room(10L, 1L, "101", 2);
        when(roomService.getById(10L)).thenReturn(existing);
        when(roomService.getOne(any(Wrapper.class))).thenReturn(existing);
        when(bedService.list(any(Wrapper.class))).thenReturn(List.of(new Bed()));
        when(roomService.saveOrUpdate(submitted)).thenReturn(true);

        RoomController controller = new RoomController(roomService, buildingService, bedService,
            stayHistoryService, mock(DormManagerScopeService.class));

        assertThat(controller.save(submitted).getCode()).isEqualTo(200);

        ArgumentCaptor<Wrapper<Room>> lockCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(roomService).getOne(lockCaptor.capture());
        assertThat(lockCaptor.getValue().getSqlSegment()).contains("FOR UPDATE");
        InOrder lockOrder = inOrder(roomService, bedService);
        lockOrder.verify(roomService).getOne(any(Wrapper.class));
        lockOrder.verify(bedService).list(any(Wrapper.class));
    }

    @Test
    void updateEmptyRoomUsesPersistedRoomNumberWhenCreatingBeds() {
        Room existing = room(10L, 1L, "101", 2);
        Room submitted = room(10L, 1L, null, 2);
        when(roomService.getById(10L)).thenReturn(existing);
        when(roomService.getOne(any(Wrapper.class))).thenReturn(existing);
        when(bedService.list(any(Wrapper.class))).thenReturn(List.of());
        when(roomService.saveOrUpdate(submitted)).thenReturn(true);
        AtomicReference<Collection<Bed>> savedBeds = new AtomicReference<>();
        when(bedService.saveBatch(any())).thenAnswer(invocation -> {
            savedBeds.set(invocation.getArgument(0));
            return true;
        });

        RoomController controller = new RoomController(roomService, buildingService, bedService,
            stayHistoryService, mock(DormManagerScopeService.class));

        assertThat(controller.save(submitted).getCode()).isEqualTo(200);

        assertThat(savedBeds.get()).extracting(Bed::getBedNumber)
            .containsExactly("101-1", "101-2");
    }

    @Test
    void createRejectsInvalidCapacityBeforeWriting() {
        Room submitted = room(null, 1L, "101", 0);

        RoomController controller = new RoomController(roomService, buildingService, bedService,
            stayHistoryService, mock(DormManagerScopeService.class));
        com.dorm.backend.common.Result<Boolean> result = controller.save(submitted);

        assertThat(result.getCode()).isEqualTo(400);
        verify(roomService, never()).saveOrUpdate(any(Room.class));
        verify(bedService, never()).saveBatch(any());
    }

    @Test
    void deleteRejectsRoomWithOccupiedBed() {
        Bed occupiedBed = new Bed();
        occupiedBed.setId(1L);
        occupiedBed.setStudentId(7L);
        occupiedBed.setStatus("OCCUPIED");
        when(roomService.getOne(any(Wrapper.class))).thenReturn(room(10L, 1L, "101", 1));
        when(bedService.list(any(Wrapper.class))).thenReturn(List.of(occupiedBed));

        RoomController controller = new RoomController(roomService, buildingService, bedService,
            stayHistoryService, mock(DormManagerScopeService.class));
        com.dorm.backend.common.Result<Boolean> result = controller.delete(10L);

        assertThat(result.getCode()).isEqualTo(400);
        verify(bedService, never()).remove(any(Wrapper.class));
        verify(roomService, never()).removeById(10L);
    }

    @Test
    void deleteRunsInsideTransaction() throws Exception {
        assertThat(RoomController.class.getMethod("delete", Long.class)
                .isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class))
            .isTrue();
    }

    @Test
    void deleteRejectsRoomWithHistoricalBed() {
        Bed historicalBed = new Bed();
        historicalBed.setId(1L);
        historicalBed.setRoomId(10L);
        historicalBed.setStatus("EMPTY");
        when(roomService.getOne(any(Wrapper.class))).thenReturn(room(10L, 1L, "101", 1));
        when(bedService.list(any(Wrapper.class))).thenReturn(List.of(historicalBed));
        when(stayHistoryService.list(any(Wrapper.class)))
            .thenReturn(List.of(new com.dorm.backend.entity.StayHistory()));

        RoomController controller = new RoomController(roomService, buildingService, bedService,
            stayHistoryService, mock(DormManagerScopeService.class));
        com.dorm.backend.common.Result<Boolean> result = controller.delete(10L);

        assertThat(result.getCode()).isEqualTo(400);
        verify(bedService, never()).remove(any(Wrapper.class));
        verify(roomService, never()).removeById(10L);
    }

    @Test
    void deleteReturns404ForMissingRoom() {
        when(roomService.getOne(any(Wrapper.class))).thenReturn(null);

        RoomController controller = new RoomController(roomService, buildingService, bedService,
            stayHistoryService, mock(DormManagerScopeService.class));
        com.dorm.backend.common.Result<Boolean> result = controller.delete(10L);

        assertThat(result.getCode()).isEqualTo(404);
        verify(bedService, never()).list(any(Wrapper.class));
        verify(roomService, never()).removeById(10L);
    }

    @Test
    void deleteLocksRoomAndBedsBeforeRemoving() {
        Room room = room(10L, 1L, "101", 2);
        Bed first = new Bed();
        first.setId(1L);
        first.setRoomId(10L);
        first.setStatus("EMPTY");
        Bed second = new Bed();
        second.setId(2L);
        second.setRoomId(10L);
        second.setStatus("BROKEN");
        when(roomService.getOne(any(Wrapper.class))).thenReturn(room);
        when(bedService.list(any(Wrapper.class))).thenReturn(List.of(first, second));
        when(stayHistoryService.list(any(Wrapper.class))).thenReturn(List.of());
        when(bedService.remove(any(Wrapper.class))).thenReturn(true);
        when(roomService.removeById(10L)).thenReturn(true);

        RoomController controller = new RoomController(roomService, buildingService, bedService,
            stayHistoryService, mock(DormManagerScopeService.class));
        com.dorm.backend.common.Result<Boolean> result = controller.delete(10L);

        assertThat(result.getCode()).isEqualTo(200);
        ArgumentCaptor<Wrapper<Room>> roomLockCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(roomService).getOne(roomLockCaptor.capture());
        assertThat(roomLockCaptor.getValue().getSqlSegment()).contains("FOR UPDATE");
        ArgumentCaptor<Wrapper<Bed>> bedLockCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(bedService).list(bedLockCaptor.capture());
        assertThat(bedLockCaptor.getValue().getSqlSegment()).contains("FOR UPDATE");
    }

    @Test
    void batchCreateGeneratesRoomsAndBeds() throws Exception {
        Building building = new Building();
        building.setId(1L);
        building.setFloors(6);
        when(buildingService.getById(1L)).thenReturn(building);
        when(roomService.list(any(Wrapper.class))).thenReturn(List.of());

        AtomicReference<Collection<Room>> savedRooms = new AtomicReference<>();
        when(roomService.saveBatch(any())).thenAnswer(invocation -> {
            Collection<Room> rooms = invocation.getArgument(0);
            long id = 10L;
            for (Room room : rooms) room.setId(id++);
            savedRooms.set(rooms);
            return true;
        });
        AtomicReference<Collection<Bed>> savedBeds = new AtomicReference<>();
        when(bedService.saveBatch(any())).thenAnswer(invocation -> {
            savedBeds.set(invocation.getArgument(0));
            return true;
        });

        mockMvc.perform(post("/api/room/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "buildingId": 1,
                      "startFloor": 1,
                      "endFloor": 2,
                      "roomsPerFloor": 2,
                      "startSequence": 1,
                      "capacity": 4
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data").value(4));

        assertThat(savedRooms.get()).extracting(Room::getRoomNumber)
            .containsExactly("101", "102", "201", "202");
        assertThat(savedBeds.get()).hasSize(16);
    }

    @Test
    void batchCreateRejectsExistingRoomNumberBeforeWriting() throws Exception {
        Building building = new Building();
        building.setId(1L);
        building.setFloors(6);
        when(buildingService.getById(1L)).thenReturn(building);
        Room existing = new Room();
        existing.setRoomNumber("102");
        when(roomService.list(any(Wrapper.class))).thenReturn(List.of(existing));

        mockMvc.perform(post("/api/room/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "buildingId": 1,
                      "startFloor": 1,
                      "endFloor": 1,
                      "roomsPerFloor": 2,
                      "startSequence": 1,
                      "capacity": 4
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value("房间号已存在：102"));

        verify(roomService, never()).saveBatch(any());
        verify(bedService, never()).saveBatch(any());
    }

    private Room room(Long id, Long buildingId, String roomNumber, Integer capacity) {
        Room room = new Room();
        room.setId(id);
        room.setBuildingId(buildingId);
        room.setRoomNumber(roomNumber);
        room.setCapacity(capacity);
        room.setStatus("NORMAL");
        return room;
    }
}
