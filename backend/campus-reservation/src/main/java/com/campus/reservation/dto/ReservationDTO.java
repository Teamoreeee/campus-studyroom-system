package com.campus.reservation.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservationDTO {
    private Long reservationId;
    private Long userId;
    private Long roomId;
    private Long seatId;
    private LocalDateTime reservationDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private String purpose;
    private String notes;
    private String qrcode;
}