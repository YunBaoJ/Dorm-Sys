package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.dorm.backend.entity.Bed;
import com.dorm.backend.entity.Building;
import com.dorm.backend.entity.Room;
import com.dorm.backend.service.BedService;
import com.dorm.backend.service.BuildingService;
import com.dorm.backend.service.DormManagerScopeService;
import com.dorm.backend.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RoomControllerTest {

    private RoomService roomService;
    private BuildingService buildingService;
    private BedService bedService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        roomService = mock(RoomService.class);
        buildingService = mock(BuildingService.class);
        bedService = mock(BedService.class);

        RoomController controller = new RoomController(roomService, buildingService, bedService, mock(DormManagerScopeService.class));
        ReflectionTestUtils.setField(controller, "roomService", roomService);
        ReflectionTestUtils.setField(controller, "buildingService", buildingService);
        ReflectionTestUtils.setField(controller, "bedService", bedService);
        ReflectionTestUtils.setField(controller, "managerScopeService", mock(DormManagerScopeService.class));
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
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
}
