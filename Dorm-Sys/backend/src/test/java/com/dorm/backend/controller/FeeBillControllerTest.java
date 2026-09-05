package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.entity.FeeBill;
import com.dorm.backend.entity.Room;
import com.dorm.backend.service.DormManagerScopeService;
import com.dorm.backend.service.FeeBillService;
import com.dorm.backend.service.RoomService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

class FeeBillControllerTest {

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void saveRunsInsideTransaction() throws Exception {
        assertThat(FeeBillController.class.getMethod("save", FeeBill.class)
                .isAnnotationPresent(Transactional.class))
            .isTrue();
    }

    @Test
    void saveLocksTargetRoomBeforeCheckingForDuplicates() {
        FeeBillService feeBillService = mock(FeeBillService.class);
        RoomService roomService = mock(RoomService.class);
        FeeBill bill = bill(null, 10L, "WATER", "2026-09");
        Room room = new Room();
        room.setId(10L);
        when(roomService.getOne(any(Wrapper.class))).thenReturn(room);
        when(feeBillService.count(any(Wrapper.class))).thenReturn(0L);
        when(feeBillService.saveOrUpdate(bill)).thenReturn(true);
        FeeBillController controller = controller(feeBillService, roomService);

        Result<Boolean> result = controller.save(bill);

        assertThat(result.getCode()).isEqualTo(200);
        ArgumentCaptor<Wrapper<Room>> roomLockCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(roomService).getOne(roomLockCaptor.capture());
        assertThat(roomLockCaptor.getValue().getSqlSegment()).contains("FOR UPDATE");
        InOrder order = inOrder(roomService, feeBillService);
        order.verify(roomService).getOne(any(Wrapper.class));
        order.verify(feeBillService).count(any(Wrapper.class));
    }

    @Test
    void managerSaveLocksTargetRoomBeforePermissionReads() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("currentUserId", 7L);
        request.setAttribute("currentUserRole", "dormmanager");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        FeeBillService feeBillService = mock(FeeBillService.class);
        RoomService roomService = mock(RoomService.class);
        DormManagerScopeService scopeService = mock(DormManagerScopeService.class);
        FeeBill bill = bill(5L, 10L, "WATER", "2026-09");
        FeeBill existing = bill(5L, 10L, "WATER", "2026-09");
        Room room = new Room();
        room.setId(10L);
        when(roomService.getOne(any(Wrapper.class))).thenReturn(room);
        when(feeBillService.getById(5L)).thenReturn(existing);
        when(scopeService.canManageRoom(7L, 10L)).thenReturn(true);
        when(feeBillService.count(any(Wrapper.class))).thenReturn(0L);
        when(feeBillService.saveOrUpdate(bill)).thenReturn(true);
        FeeBillController controller = new FeeBillController(feeBillService, roomService, scopeService);

        Result<Boolean> result = controller.save(bill);

        assertThat(result.getCode()).isEqualTo(200);
        InOrder order = inOrder(roomService, feeBillService, scopeService);
        order.verify(roomService).getOne(any(Wrapper.class));
        order.verify(feeBillService).getById(5L);
        order.verify(scopeService, times(2)).canManageRoom(7L, 10L);
        order.verify(feeBillService).count(any(Wrapper.class));
    }

    @Test
    void createRejectsDuplicateRoomTypeAndMonth() {
        FeeBillService feeBillService = mock(FeeBillService.class);
        FeeBill bill = bill(null, 10L, "WATER", "2026-09");
        when(feeBillService.count(any(Wrapper.class))).thenReturn(1L);

        Result<Boolean> result = controller(feeBillService).save(bill);

        assertThat(result.getCode()).isEqualTo(409);
        assertThat(result.getMessage()).isEqualTo("该房间本月同类型账单已存在");
        verify(feeBillService, never()).saveOrUpdate(any(FeeBill.class));
    }

    @Test
    void updateAllowsKeepingItsOwnBusinessKey() {
        FeeBillService feeBillService = mock(FeeBillService.class);
        FeeBill bill = bill(5L, 10L, "WATER", "2026-09");
        when(feeBillService.count(any(Wrapper.class))).thenReturn(0L);
        when(feeBillService.saveOrUpdate(bill)).thenReturn(true);

        Result<Boolean> result = controller(feeBillService).save(bill);

        assertThat(result.getCode()).isEqualTo(200);
        ArgumentCaptor<Wrapper<FeeBill>> queryCaptor = ArgumentCaptor.forClass(Wrapper.class);
        verify(feeBillService).count(queryCaptor.capture());
        QueryWrapper<FeeBill> duplicateQuery = (QueryWrapper<FeeBill>) queryCaptor.getValue();
        assertThat(duplicateQuery.getSqlSegment()).contains("id <>");
        assertThat(duplicateQuery.getParamNameValuePairs().values())
            .contains(5L, 10L, "WATER", "2026-09");
        verify(feeBillService).saveOrUpdate(bill);
    }

    private FeeBillController controller(FeeBillService feeBillService) {
        RoomService roomService = mock(RoomService.class);
        Room room = new Room();
        room.setId(10L);
        when(roomService.getOne(any(Wrapper.class))).thenReturn(room);
        return controller(feeBillService, roomService);
    }

    private FeeBillController controller(FeeBillService feeBillService, RoomService roomService) {
        return new FeeBillController(feeBillService, roomService,
            mock(DormManagerScopeService.class));
    }

    private FeeBill bill(Long id, Long roomId, String type, String month) {
        FeeBill bill = new FeeBill();
        bill.setId(id);
        bill.setRoomId(roomId);
        bill.setType(type);
        bill.setMonth(month);
        return bill;
    }
}
