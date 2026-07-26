package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.dorm.backend.entity.Bed;
import com.dorm.backend.entity.TransferRequest;
import com.dorm.backend.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransferRequestControllerTest {

    @Test
    void approveMovesStudentToAvailableBedInTargetRoom() {
        TransferRequestService transferRequestService = mock(TransferRequestService.class);
        BedService bedService = mock(BedService.class);

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

        when(bedService.getById(1L)).thenReturn(currentBed);
        when(bedService.list(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any()))
            .thenReturn(List.of(targetBed), List.of(currentBed), List.of(targetBed));
        when(transferRequestService.saveOrUpdate(any())).thenReturn(true);

        TransferRequestController controller = new TransferRequestController(transferRequestService, mock(UserService.class),
            bedService, mock(RoomService.class), mock(BuildingService.class), mock(DormManagerScopeService.class));
        ReflectionTestUtils.setField(controller, "transferRequestService", transferRequestService);
        ReflectionTestUtils.setField(controller, "bedService", bedService);
        ReflectionTestUtils.setField(controller, "userService", mock(UserService.class));
        ReflectionTestUtils.setField(controller, "roomService", mock(RoomService.class));
        ReflectionTestUtils.setField(controller, "buildingService", mock(BuildingService.class));

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
        verify(bedService, times(2)).update(org.mockito.ArgumentMatchers.<Wrapper<Bed>>any());
        verify(bedService).updateById(targetBed);
        verify(transferRequestService).saveOrUpdate(request);
    }
}
