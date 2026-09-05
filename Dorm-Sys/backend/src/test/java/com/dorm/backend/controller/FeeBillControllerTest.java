package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.entity.FeeBill;
import com.dorm.backend.service.DormManagerScopeService;
import com.dorm.backend.service.FeeBillService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;

class FeeBillControllerTest {

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
        return new FeeBillController(feeBillService, mock(DormManagerScopeService.class));
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
