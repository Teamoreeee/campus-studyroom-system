package com.campus.ai.entity;

import lombok.Data;

@Data
public class RoomInfo {

    private Long roomId;

    private String roomName;

    private String building;

    private Integer capacity;
}
