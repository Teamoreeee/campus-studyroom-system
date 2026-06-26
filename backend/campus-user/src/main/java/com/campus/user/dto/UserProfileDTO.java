package com.campus.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserProfileDTO {
    private Long profileId;
    private Long userId;
    private String nickname;
    private String avatar;
    private Integer gender;
    private String phone;
    private String email;
    private String department;
    private String major;
    private String grade;
    private String studyPreferences;
    private Integer loginCount;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}