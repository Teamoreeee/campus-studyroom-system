package com.campus.reservation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("reservation_history")
public class ReservationHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "history_id", type = IdType.AUTO)
    private Long historyId;

    @TableField("reservation_id")
    private Long reservationId;

    @TableField("user_id")
    private Long userId;

    @TableField("room_id")
    private Long roomId;

    @TableField("seat_id")
    private Long seatId;

    @TableField("reservation_date")
    private LocalDateTime reservationDate;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("status")
    private String status;

    @TableField("purpose")
    private String purpose;

    @TableField("notes")
    private String notes;

    @TableField("qrcode")
    private String qrcode;

    @TableField("action")
    private String action; // create, confirm, cancel, complete, timeout

    @TableField("action_time")
    private LocalDateTime actionTime;

    @TableField("action_user")
    private Long actionUser;

    @TableField("create_time")
    private LocalDateTime createTime;
}