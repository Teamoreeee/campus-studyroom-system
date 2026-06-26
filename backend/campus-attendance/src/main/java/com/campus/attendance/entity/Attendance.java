package com.campus.attendance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("attendance")
public class Attendance implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "attendance_id", type = IdType.AUTO)
    private Long attendanceId;

    @TableField("reservation_id")
    private Long reservationId;

    @TableField("user_id")
    private Long userId;

    @TableField("room_id")
    private Long roomId;

    @TableField("seat_id")
    private Long seatId;

    @TableField("check_in_time")
    private LocalDateTime checkInTime;

    @TableField("check_out_time")
    private LocalDateTime checkOutTime;

    @TableField("duration_minutes")
    private Integer durationMinutes;

    @TableField("current_count")
    private Integer currentCount;

    @TableField("status")
    private String status; // active, completed, timeout, cancelled

    @TableField("check_in_method")
    private String checkInMethod; // qrcode, manual, auto

    @TableField("check_out_method")
    private String checkOutMethod; // qrcode, manual, auto

    @TableField("location")
    private String location;

    @TableField("device_info")
    private String deviceInfo;

    @TableField("notes")
    private String notes;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}