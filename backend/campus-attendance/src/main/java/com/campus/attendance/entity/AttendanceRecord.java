package com.campus.attendance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("attendance_record")
public class AttendanceRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "record_id", type = IdType.AUTO)
    private Long recordId;

    @TableField("attendance_id")
    private Long attendanceId;

    @TableField("user_id")
    private Long userId;

    @TableField("room_id")
    private Long roomId;

    @TableField("seat_id")
    private Long seatId;

    @TableField("action")
    private String action; // check_in, check_out, extend, timeout

    @TableField("action_time")
    private LocalDateTime actionTime;

    @TableField("action_user")
    private Long actionUser;

    @TableField("device_info")
    private String deviceInfo;

    @TableField("location")
    private String location;

    @TableField("notes")
    private String notes;

    @TableField("create_time")
    private LocalDateTime createTime;
}