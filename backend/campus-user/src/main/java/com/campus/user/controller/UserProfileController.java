package com.campus.user.controller;

import com.campus.user.dto.UserProfileDTO;
import com.campus.user.entity.UserProfile;
import com.campus.user.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/profile")
@RequiredArgsConstructor
@Tag(name = "用户档案管理", description = "用户个人信息管理")
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/{userId}")
    @Operation(summary = "获取用户档案")
    public ResponseEntity<UserProfileDTO> getProfile(@PathVariable Long userId) {
        UserProfile profile = userProfileService.getByUserId(userId);
        if (profile == null) {
            return ResponseEntity.notFound().build();
        }

        UserProfileDTO dto = new UserProfileDTO();
        BeanUtils.copyProperties(profile, dto);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{userId}")
    @Operation(summary = "更新用户档案")
    public ResponseEntity<String> updateProfile(@PathVariable Long userId, @RequestBody UserProfileDTO dto) {
        UserProfile profile = new UserProfile();
        BeanUtils.copyProperties(dto, profile);
        profile.setUserId(userId);

        boolean success = userProfileService.saveOrUpdateProfile(profile);
        if (success) {
            return ResponseEntity.ok("更新成功");
        } else {
            return ResponseEntity.badRequest().body("更新失败");
        }
    }

    @PutMapping("/{userId}/preferences")
    @Operation(summary = "更新学习偏好")
    public ResponseEntity<String> updatePreferences(@PathVariable Long userId, @RequestBody String preferences) {
        boolean success = userProfileService.updateStudyPreferences(userId, preferences);
        if (success) {
            return ResponseEntity.ok("偏好更新成功");
        } else {
            return ResponseEntity.badRequest().body("偏好更新失败");
        }
    }

    @PostMapping("/{userId}/login")
    @Operation(summary = "更新登录信息")
    public ResponseEntity<String> updateLoginInfo(@PathVariable Long userId) {
        boolean success = userProfileService.updateLoginInfo(userId);
        if (success) {
            return ResponseEntity.ok("登录信息更新成功");
        } else {
            return ResponseEntity.badRequest().body("登录信息更新失败");
        }
    }
}