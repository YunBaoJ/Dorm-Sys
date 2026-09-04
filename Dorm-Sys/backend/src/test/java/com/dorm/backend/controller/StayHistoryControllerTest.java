package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dorm.backend.entity.Bed;
import com.dorm.backend.entity.StayHistory;
import com.dorm.backend.service.BedService;
import com.dorm.backend.service.DormManagerScopeService;
import com.dorm.backend.service.StayHistoryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StayHistoryControllerTest {

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void managerListIsLimitedToBedsInManagedRooms() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("currentUserId", 7L);
        request.setAttribute("currentUserRole", "dormmanager");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        StayHistoryService historyService = mock(StayHistoryService.class);
        BedService bedService = mock(BedService.class);
        DormManagerScopeService scopeService = mock(DormManagerScopeService.class);
        when(scopeService.managedRoomIds(7L)).thenReturn(List.of(1L));

        Bed managedBed = new Bed();
        managedBed.setId(10L);
        managedBed.setRoomId(1L);
        when(bedService.list(any(QueryWrapper.class))).thenReturn(List.of(managedBed));

        StayHistory managedHistory = new StayHistory();
        managedHistory.setId(100L);
        managedHistory.setBedId(10L);
        StayHistory foreignHistory = new StayHistory();
        foreignHistory.setId(200L);
        foreignHistory.setBedId(99L);
        when(historyService.page(any(Page.class), any(QueryWrapper.class))).thenAnswer(invocation -> {
            QueryWrapper<StayHistory> query = invocation.getArgument(1);
            boolean scopedToManagedBed = query.getSqlSegment().contains("bed_id");
            List<StayHistory> records = scopedToManagedBed
                    ? List.of(managedHistory)
                    : List.of(managedHistory, foreignHistory);
            return new Page<StayHistory>().setRecords(records);
        });

        Constructor<?> constructor = Arrays.stream(StayHistoryController.class.getConstructors())
                .filter(candidate -> Arrays.equals(candidate.getParameterTypes(), new Class<?>[]{
                        StayHistoryService.class, BedService.class, DormManagerScopeService.class
                }))
                .findFirst()
                .orElseThrow(() -> new AssertionError("StayHistoryController must receive scope dependencies"));
        StayHistoryController controller = (StayHistoryController) constructor.newInstance(
                historyService, bedService, scopeService);

        assertThat(controller.list(1, 100).getData())
                .extracting(StayHistory::getId)
                .containsExactly(100L);
    }
}
