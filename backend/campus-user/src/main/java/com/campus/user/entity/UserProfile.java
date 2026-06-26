package com.campus.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user_profile")
public class UserProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "profile_id", type = IdType.AUTO)
    private Long profileId;

    @TableField("user_id")
    private Long userId;

    @TableField("nickname")
    private String nickname;

    @TableField("avatar")
    private String avatar;

    @TableField("gender")
    private Integer gender;

    @TableField("phone")
    private String phone;

    @TableField("email")
    private String email;

    @TableField("department")
    private String department;

    @TableField("major")
    private String major;

    @TableField("grade")
    private String grade;

    @TableField("study_preferences")
    private String studyPreferences;

    @TableField("login_count")
    private Integer loginCount;

    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}