package com.dorm.backend.dto;

import lombok.Data;

@Data
public class RoomBatchCreateRequest {
    private Long buildingId;
    private Integer startFloor;
    private Integer endFloor;
    private Integer roomsPerFloor;
    private Integer startSequence;
    private Integer capacity;
}
