package com.dorm.backend.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    @Test
    void bedAllocationConflictReturnsBusinessConflict() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        Result<Void> result = handler.handleBedAllocationConflict(
            new BedAllocationConflictException("床位状态已变化，请刷新后重试"));

        assertThat(result.getCode()).isEqualTo(409);
        assertThat(result.getMessage()).isEqualTo("床位状态已变化，请刷新后重试");
    }
}
