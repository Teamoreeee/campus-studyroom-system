package com.campus.ai.entity;

import lombok.Data;

@Data
public class ReservationRecord {

    private Long userId;

    private Long roomId;

    private Long seatId;

    private Integer count;
}
