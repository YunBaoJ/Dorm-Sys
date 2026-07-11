package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.dorm.backend.entity.Bed;
import com.dorm.backend.entity.RepairRequest;
import com.dorm.backend.service.BedService;
import com.dorm.backend.service.BuildingService;
import com.dorm.backend.service.RepairRequestService;
import com.dorm.backend.service.RoomService;
import com.dorm.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RepairRequestControllerTest {

    @Test
    void saveUsesSubmittersCurrentRoomWhenRoomIdMissing() {
        RepairRequestService repairRequestService = mock(RepairRequestService.class);
        BedService bedService = mock(BedService.class);
        when(repairRequestService.saveOrUpdate(any())).thenReturn(true);

        Bed bed = new Bed();
        bed.setStudentId(1L);
        bed.setRoomId(4L);
        when(bedService.list(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any())).thenReturn(List.of(bed));

        RepairRequestController controller = new RepairRequestController();
        ReflectionTestUtils.setField(controller, "repairRequestService", repairRequestService);
        ReflectionTestUtils.setField(controller, "bedService", bedService);
        ReflectionTestUtils.setField(controller, "userService", mock(UserService.class));
        ReflectionTestUtils.setField(controller, "roomService", mock(RoomService.class));
        ReflectionTestUtils.setField(controller, "buildingService", mock(BuildingService.class));

        RepairRequest request = new RepairRequest();
        request.setSubmitterId(1L);
        request.setType("网络");
        request.setDescription("网络不稳定");

        controller.save(request);

        ArgumentCaptor<RepairRequest> captor = ArgumentCaptor.forClass(RepairRequest.class);
        verify(repairRequestService).saveOrUpdate(captor.capture());
        assertThat(captor.getValue().getRoomId()).isEqualTo(4L);
    }
}
