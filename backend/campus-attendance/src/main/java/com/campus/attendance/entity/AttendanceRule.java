package com.campus.attendance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("attendance_rule")
public class AttendanceRule implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "rule_id", type = IdType.AUTO)
    private Long ruleId;

    @TableField("rule_name")
    private String ruleName;

    @TableField("room_id")
    private Long roomId;

    @TableField("start_time")
    private LocalTime startTime;

    @TableField("end_time")
    private LocalTime endTime;

    @TableField("max_duration")
    private Integer maxDuration; // 最大时长（分钟）

    @TableField("min_duration")
    private Integer minDuration; // 最小时长（分钟）

    @TableField("allow_extend")
    private Boolean allowExtend; // 是否允许延长

    @TableField("max_extend_times")
    private Integer maxExtendTimes; // 最大延长次数

    @TableField("max_extend_duration")
    private Integer maxExtendDuration; // 最大延长时长（分钟）

    @TableField("auto_check_out")
    private Boolean autoCheckOut; // 是否自动签退

    @TableField("check_out_delay")
    private Integer checkOutDelay; // 自动签退延迟（分钟）

    @TableField("status")
    private String status; // active, inactive

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}