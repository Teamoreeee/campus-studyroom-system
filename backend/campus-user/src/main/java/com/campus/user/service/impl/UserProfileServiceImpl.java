package com.campus.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.user.entity.UserProfile;
import com.campus.user.mapper.UserProfileMapper;
import com.campus.user.service.UserProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserProfileServiceImpl extends ServiceImpl<UserProfileMapper, UserProfile> implements UserProfileService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateProfile(UserProfile profile) {
        profile.setUpdateTime(LocalDateTime.now());

        // 如果用户档案已存在，则更新，否则创建
        UserProfile existing = getByUserId(profile.getUserId());
        if (existing != null) {
            profile.setProfileId(existing.getProfileId());
            return updateById(profile);
        } else {
            return save(profile);
        }
    }

    @Override
    public UserProfile getByUserId(Long userId) {
        return getOne(new QueryWrapper<UserProfile>().eq("user_id", userId).eq("deleted", 0));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStudyPreferences(Long userId, String preferences) {
        UserProfile profile = getByUserId(userId);
        if (profile == null) {
            throw new RuntimeException("用户档案不存在");
        }

        profile.setStudyPreferences(preferences);
        profile.setUpdateTime(LocalDateTime.now());
        return updateById(profile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateLoginInfo(Long userId) {
        UserProfile profile = getByUserId(userId);
        if (profile == null) {
            // 如果用户档案不存在，创建一个默认的
            profile = new UserProfile();
            profile.setUserId(userId);
            profile.setNickname("用户" + userId);
            profile.setLoginCount(1);
            profile.setLastLoginTime(LocalDateTime.now());
            return save(profile);
        }

        profile.setLoginCount(profile.getLoginCount() + 1);
        profile.setLastLoginTime(LocalDateTime.now());
        profile.setUpdateTime(LocalDateTime.now());
        return updateById(profile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disableUser(Long userId) {
        UserProfile profile = getByUserId(userId);
        if (profile == null) {
            throw new RuntimeException("用户档案不存在");
        }

        // 这里需要调用auth服务禁用用户
        // profile.setStatus(0);
        profile.setUpdateTime(LocalDateTime.now());
        return updateById(profile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enableUser(Long userId) {
        UserProfile profile = getByUserId(userId);
        if (profile == null) {
            throw new RuntimeException("用户档案不存在");
        }

        // 这里需要调用auth服务启用用户
        // profile.setStatus(1);
        profile.setUpdateTime(LocalDateTime.now());
        return updateById(profile);
    }
}