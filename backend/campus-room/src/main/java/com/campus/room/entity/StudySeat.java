package com.campus.room.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("study_seat")
public class StudySeat implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "seat_id", type = IdType.AUTO)
    private Long seatId;

    @TableField("room_id")
    private Long roomId;

    @TableField("seat_number")
    private String seatNumber;

    @TableField("zone")
    private String zone; // A区, B区, C区等

    @TableField("position_x")
    private Integer positionX;

    @TableField("position_y")
    private Integer positionY;

    @TableField("status")
    private String status; // available, occupied, reserved, maintenance

    @TableField("type")
    private String type; // normal, power, window, corner

    @TableField("has_power")
    private Boolean hasPower;

    @TableField("has_usb")
    private Boolean hasUsb;

    @TableField("has_wifi")
    private Boolean hasWifi;

    @TableField("description")
    private String description;

    @TableField("current_user_id")
    private Long currentUserId;

    @TableField("current_reservation_id")
    private Long currentReservationId;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}