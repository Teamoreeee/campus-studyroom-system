package com.campus.room.dto;

import lombok.Data;

@Data
public class RoomDTO {
    private Long roomId;
    private String roomName;
    private String building;
    private Integer floor;
    private Integer capacity;
    private Integer currentCount;
    private String status;
    private String description;
    private String facilities;
    private String openTime;
}