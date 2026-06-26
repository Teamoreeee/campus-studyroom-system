package com.campus.ai.entity;

import lombok.Data;

@Data
public class SeatInfo {

    private Long seatId;

    private Long roomId;

    private String seatNumber;

    private String type;

    private Boolean hasPower;
}
