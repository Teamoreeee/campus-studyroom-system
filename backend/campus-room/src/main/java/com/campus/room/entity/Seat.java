package com.campus.room.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("seat")
public class Seat implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "seat_id", type = IdType.AUTO)
    private Long seatId;

    @TableField("room_id")
    private Long roomId;

    @TableField("seat_number")
    private String seatNumber;

    @TableField("seat_type")
    private String seatType; // window, wall, power, normal

    @TableField("status")
    private String status; // available, reserved, in_use, maintenance

    @TableField("position_x")
    private Integer positionX;

    @TableField("position_y")
    private Integer positionY;

    @TableField("zone")
    private String zone;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}