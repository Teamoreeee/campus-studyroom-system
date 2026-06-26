package com.campus.auth.controller;

import com.campus.auth.dto.LoginRequest;
import com.campus.auth.dto.LoginResponse;
import com.campus.auth.dto.UserInfo;
import com.campus.auth.entity.User;
import com.campus.auth.service.UserService;
import com.campus.auth.utils.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "认证管理", description = "用户登录、注册、token管理")
public class AuthController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        // 用户登录验证
        User user = userService.login(request.getUsername(), request.getPassword());

        // 生成token
        String accessToken = jwtUtils.generateAccessToken(user.getUserId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtils.generateRefreshToken(user.getUserId(), user.getUsername());

        // 构建响应
        LoginResponse response = new LoginResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);

        UserInfo userInfo = new UserInfo();
        BeanUtils.copyProperties(user, userInfo);
        response.setUserInfo(userInfo);

        return response;
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public String register(@Valid @RequestBody RegisterRequest request) {
        userService.register(
                request.getUsername(),
                request.getPassword(),
                request.getRealName(),
                request.getStudentNo(),
                request.getEmail()
        );
        return "注册成功";
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新access token")
    public LoginResponse refresh(@RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // 验证refresh token
        if (!jwtUtils.validateToken(refreshToken) || !jwtUtils.isRefreshToken(refreshToken)) {
            throw new RuntimeException("无效的refresh token");
        }

        // 生成新的access token
        Long userId = jwtUtils.getUserIdFromToken(refreshToken);
        String username = jwtUtils.getUsernameFromToken(refreshToken);
        String role = jwtUtils.getRoleFromToken(refreshToken);

        String newAccessToken = jwtUtils.generateAccessToken(userId, username, role);
        String newRefreshToken = jwtUtils.generateRefreshToken(userId, username);

        // 构建响应
        LoginResponse response = new LoginResponse();
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(newRefreshToken);

        return response;
    }

    @GetMapping("/profile")
    @Operation(summary = "获取用户信息")
    public UserInfo getUserProfile(@RequestHeader("Authorization") String token) {
        // 解析token获取用户ID
        String jwtToken = token.replace("Bearer ", "");
        Long userId = jwtUtils.getUserIdFromToken(jwtToken);

        // 获取用户信息
        User user = userService.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        UserInfo userInfo = new UserInfo();
        BeanUtils.copyProperties(user, userInfo);
        return userInfo;
    }

    @PutMapping("/password")
    @Operation(summary = "修改密码")
    public String changePassword(@RequestHeader("Authorization") String token,
                               @Valid @RequestBody ChangePasswordRequest request) {
        // 解析token获取用户ID
        String jwtToken = token.replace("Bearer ", "");
        Long userId = jwtUtils.getUserIdFromToken(jwtToken);

        // 修改密码
        userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        return "密码修改成功";
    }

    // 请求DTO类
    public static class RegisterRequest {
        private String username;
        private String password;
        private String realName;
        private String studentNo;
        private String email;

        // getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getRealName() { return realName; }
        public void setRealName(String realName) { this.realName = realName; }
        public String getStudentNo() { return studentNo; }
        public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class RefreshTokenRequest {
        private String refreshToken;

        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }

    public static class ChangePasswordRequest {
        private String oldPassword;
        private String newPassword;

        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}