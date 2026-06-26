package com.campus.auth.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserInfo {
    private Long userId;
    private String username;
    private String realName;
    private String studentNo;
    private String email;
    private String phone;
    private String avatar;
    private String role;
    private Integer status;
    private LocalDateTime createTime;
}