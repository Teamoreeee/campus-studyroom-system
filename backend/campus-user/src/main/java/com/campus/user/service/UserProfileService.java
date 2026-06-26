package com.campus.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.user.entity.UserProfile;

public interface UserProfileService extends IService<UserProfile> {

    /**
     * 创建或更新用户档案
     */
    boolean saveOrUpdateProfile(UserProfile profile);

    /**
     * 根据用户ID获取用户档案
     */
    UserProfile getByUserId(Long userId);

    /**
     * 更新用户学习偏好
     */
    boolean updateStudyPreferences(Long userId, String preferences);

    /**
     * 更新登录信息
     */
    boolean updateLoginInfo(Long userId);

    /**
     * 禁用用户
     */
    boolean disableUser(Long userId);

    /**
     * 启用用户
     */
    boolean enableUser(Long userId);
}