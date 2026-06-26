package com.campus.room.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("study_room")
public class StudyRoom implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "room_id", type = IdType.AUTO)
    private Long roomId;

    @TableField("room_name")
    private String roomName;

    @TableField("building")
    private String building;

    @TableField("floor")
    private Integer floor;

    @TableField("capacity")
    private Integer capacity;

    @TableField("current_count")
    private Integer currentCount;

    @TableField("status")
    private String status; // open, closed, maintenance

    @TableField("description")
    private String description;

    @TableField("facilities")
    private String facilities; // JSON格式存储设施信息

    @TableField("open_time")
    private String openTime; // JSON格式存储开放时间段

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}