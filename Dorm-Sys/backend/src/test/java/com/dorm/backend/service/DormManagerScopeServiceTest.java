package com.dorm.backend.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.dorm.backend.entity.Building;
import com.dorm.backend.entity.ManagerInfo;
import com.dorm.backend.entity.Room;
import com.dorm.backend.entity.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DormManagerScopeServiceTest {

    @Test
    void usesStructuredManagerAssignmentWhenPresent() {
        ManagerInfoService managerInfoService = mock(ManagerInfoService.class);
        ManagerInfo info = new ManagerInfo();
        info.setUserId(7L);
        info.setBuildingId(3L);
        when(managerInfoService.list(any(Wrapper.class))).thenReturn(List.of(info));

        DormManagerScopeService service = new DormManagerScopeService(
            managerInfoService, mock(UserService.class), mock(BuildingService.class), mock(RoomService.class), mock(BedService.class));

        assertThat(service.managedBuildingIds(7L)).containsExactly(3L);
    }

    @Test
    void fallsBackToBuildingManagerNameAndResolvesRooms() {
        ManagerInfoService managerInfoService = mock(ManagerInfoService.class);
        UserService userService = mock(UserService.class);
        BuildingService buildingService = mock(BuildingService.class);
        RoomService roomService = mock(RoomService.class);
        when(managerInfoService.list(any(Wrapper.class))).thenReturn(List.of());

        User manager = new User();
        manager.setId(7L);
        manager.setName("周老师");
        when(userService.getById(7L)).thenReturn(manager);

        Building building = new Building();
        building.setId(3L);
        when(buildingService.list(any(Wrapper.class))).thenReturn(List.of(building));

        Room room = new Room();
        room.setId(10L);
        room.setBuildingId(3L);
        when(roomService.list(any(Wrapper.class))).thenReturn(List.of(room));

        DormManagerScopeService service = new DormManagerScopeService(
            managerInfoService, userService, buildingService, roomService, mock(BedService.class));

        assertThat(service.managedBuildingIds(7L)).containsExactly(3L);
        assertThat(service.managedRoomIds(7L)).containsExactly(10L);
        assertThat(service.canManageRoom(7L, 10L)).isTrue();
        assertThat(service.canManageRoom(7L, 11L)).isFalse();
    }
}
