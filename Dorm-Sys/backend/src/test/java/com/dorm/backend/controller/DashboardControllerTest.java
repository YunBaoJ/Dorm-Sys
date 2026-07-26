package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.entity.Bed;
import com.dorm.backend.entity.Building;
import com.dorm.backend.entity.Room;
import com.dorm.backend.entity.User;
import com.dorm.backend.service.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DashboardControllerTest {

    @AfterEach
    void clearRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void dormDashboardReturnsOnlyCurrentDormitorySummary() {
        BedService bedService = mock(BedService.class);
        RoomService roomService = mock(RoomService.class);
        BuildingService buildingService = mock(BuildingService.class);
        UserService userService = mock(UserService.class);

        Bed mine = bed(1L, 10L, 7L, "101-1");
        Bed roommateBed = bed(2L, 10L, 8L, "101-2");
        when(bedService.getOne(any(Wrapper.class))).thenReturn(mine);
        when(bedService.list(any(Wrapper.class))).thenReturn(List.of(mine, roommateBed));

        Room room = new Room();
        room.setId(10L);
        room.setBuildingId(3L);
        room.setRoomNumber("101");
        when(roomService.getById(10L)).thenReturn(room);

        Building building = new Building();
        building.setId(3L);
        building.setName("至善楼");
        when(buildingService.getById(3L)).thenReturn(building);

        User roommate = new User();
        roommate.setId(8L);
        roommate.setName("李明");
        roommate.setPassword("not-for-client");
        when(userService.list(any(Wrapper.class))).thenReturn(List.of(roommate));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("currentUserId", 7L);
        request.setAttribute("currentUserRole", "student");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        DashboardController controller = new DashboardController(mock(FeeBillService.class), mock(RepairRequestService.class),
            mock(HygieneRecordService.class), mock(VisitorRecordService.class),
            bedService, roomService, buildingService, userService,
            mock(DormManagerScopeService.class));

        Result<Map<String, Object>> result = controller.getDormDashboard();

        assertThat(result.getData()).containsKeys("myBed", "room", "building", "roommates");
        List<Map<String, Object>> roommates = (List<Map<String, Object>>) result.getData().get("roommates");
        assertThat(roommates).hasSize(1);
        assertThat(roommates.get(0)).containsEntry("name", "李明").doesNotContainKey("password");
    }

    @Test
    void unassignedStudentGetsEmptyDormitorySummaryInsteadOfServerError() {
        BedService bedService = mock(BedService.class);
        when(bedService.getOne(any(Wrapper.class))).thenReturn(null);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("currentUserId", 7L);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        DashboardController controller = new DashboardController(mock(FeeBillService.class), mock(RepairRequestService.class),
            mock(HygieneRecordService.class), mock(VisitorRecordService.class),
            bedService, mock(RoomService.class), mock(BuildingService.class),
            mock(UserService.class), mock(DormManagerScopeService.class));

        Result<Map<String, Object>> result = controller.getDormDashboard();

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).containsEntry("myBed", null).containsEntry("roommates", List.of());
    }

    @Test
    void adminStatsComeFromDatabaseCounts() {
        UserService userService = mock(UserService.class);
        BuildingService buildingService = mock(BuildingService.class);
        RoomService roomService = mock(RoomService.class);
        when(userService.count(any(Wrapper.class))).thenReturn(24L, 2L);
        when(buildingService.count()).thenReturn(2L);
        when(roomService.count()).thenReturn(16L);

        DashboardController controller = new DashboardController(mock(FeeBillService.class), mock(RepairRequestService.class),
            mock(HygieneRecordService.class), mock(VisitorRecordService.class),
            mock(BedService.class), roomService, buildingService, userService,
            mock(DormManagerScopeService.class));

        Result<Map<String, Object>> result = controller.getStats();

        assertThat(result.getData())
            .containsEntry("studentCount", 24L)
            .containsEntry("managerCount", 2L)
            .containsEntry("buildingCount", 2L)
            .containsEntry("roomCount", 16L);
    }

    @Test
    void adminAlertsReportPendingRepairs() {
        RepairRequestService repairService = mock(RepairRequestService.class);
        when(repairService.count(any(Wrapper.class))).thenReturn(3L);

        DashboardController controller = new DashboardController(mock(FeeBillService.class), repairService,
            mock(HygieneRecordService.class), mock(VisitorRecordService.class),
            mock(BedService.class), mock(RoomService.class), mock(BuildingService.class),
            mock(UserService.class), mock(DormManagerScopeService.class));

        Result<List<Map<String, Object>>> result = controller.getAlerts();

        assertThat(result.getData()).anySatisfy(alert -> {
            assertThat(alert.get("title")).isEqualTo("当前有 3 条报修工单待处理");
            assertThat(alert.get("url")).isEqualTo("/admin/repairs");
        });
    }

    @Test
    void managerBuildingStatsOnlyContainAssignedBuildings() {
        BuildingService buildingService = mock(BuildingService.class);
        RoomService roomService = mock(RoomService.class);
        BedService bedService = mock(BedService.class);
        DormManagerScopeService scopeService = mock(DormManagerScopeService.class);
        Building mine = new Building();
        mine.setId(1L);
        mine.setName("明德楼");
        mine.setTotalRooms(2);
        mine.setOccupiedRooms(1);
        mine.setFreeRooms(1);
        Building other = new Building();
        other.setId(2L);
        other.setName("至善楼");
        other.setTotalRooms(3);
        other.setOccupiedRooms(2);
        other.setFreeRooms(1);
        when(buildingService.getBuildingsWithStats(any())).thenReturn(List.of(mine));
        when(roomService.list(any(Wrapper.class))).thenReturn(List.of());
        when(bedService.list(any(Wrapper.class))).thenReturn(List.of());
        when(scopeService.managedBuildingIds(7L)).thenReturn(List.of(1L));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("currentUserId", 7L);
        request.setAttribute("currentUserRole", "dormmanager");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        DashboardController controller = new DashboardController(mock(FeeBillService.class), mock(RepairRequestService.class),
            mock(HygieneRecordService.class), mock(VisitorRecordService.class),
            bedService, roomService, buildingService, mock(UserService.class),
            scopeService);

        Result<List<Map<String, Object>>> result = controller.getBuildingStats();

        assertThat(result.getData()).extracting(item -> item.get("id")).containsExactly(1L);
    }

    @Test
    void buildingStatsIncludeOccupancyPercentageAndStatus() {
        BuildingService buildingService = mock(BuildingService.class);
        RoomService roomService = mock(RoomService.class);
        BedService bedService = mock(BedService.class);

        Building building = new Building();
        building.setId(1L);
        building.setName("Test Building");
        building.setTotalRooms(1);
        Room room = new Room();
        room.setId(10L);
        room.setBuildingId(1L);
        when(buildingService.getBuildingsWithStats(any())).thenReturn(List.of(building));
        when(roomService.list(any(Wrapper.class))).thenReturn(List.of(room));
        when(bedService.list(any(Wrapper.class))).thenReturn(List.of(
            bed(1L, 10L, 7L, "101-1"),
            bed(2L, 10L, null, "101-2"),
            bed(3L, 10L, null, "101-3"),
            bed(4L, 10L, null, "101-4")
        ));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("currentUserId", 1L);
        request.setAttribute("currentUserRole", "admin");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        DashboardController controller = new DashboardController(mock(FeeBillService.class), mock(RepairRequestService.class),
            mock(HygieneRecordService.class), mock(VisitorRecordService.class),
            bedService, roomService, buildingService, mock(UserService.class),
            mock(DormManagerScopeService.class));

        Result<List<Map<String, Object>>> result = controller.getBuildingStats();

        assertThat(result.getData()).singleElement().satisfies(stat -> {
            assertThat(stat).containsEntry("occupiedBeds", 1L);
            assertThat(stat).containsEntry("totalBeds", 4);
            assertThat(stat).containsEntry("percentage", 25);
            assertThat(stat.get("status")).isEqualTo("正常");
        });
    }

    private Bed bed(Long id, Long roomId, Long studentId, String number) {
        Bed bed = new Bed();
        bed.setId(id);
        bed.setRoomId(roomId);
        bed.setStudentId(studentId);
        bed.setBedNumber(number);
        return bed;
    }
}
