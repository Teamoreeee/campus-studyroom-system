package com.campus.room.dto;

import lombok.Data;

@Data
public class SeatDTO {
    private Long seatId;
    private Long roomId;
    private String seatNumber;
    private String zone;
    private Integer positionX;
    private Integer positionY;
    private String status;
    private String type;
    private Boolean hasPower;
    private Boolean hasUsb;
    private Boolean hasWifi;
    private String description;
    private Long currentUserId;
    private Long currentReservationId;
}