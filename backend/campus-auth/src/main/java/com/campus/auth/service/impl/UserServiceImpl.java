package com.campus.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.auth.entity.User;
import com.campus.auth.mapper.UserMapper;
import com.campus.auth.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean register(String username, String password, String realName, String studentNo, String email) {
        // 检查用户名是否已存在
        if (getByUsername(username) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查学号是否已存在
        if (getByStudentNo(studentNo) != null) {
            throw new RuntimeException("学号已存在");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRealName(realName);
        user.setStudentNo(studentNo);
        user.setEmail(email);
        user.setRole("student"); // 默认为学生角色
        user.setStatus(1); // 默认启用

        return save(user);
    }

    @Override
    public User login(String username, String password) {
        User user = getByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (user.getStatus() != 1) {
            throw new RuntimeException("用户已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        return user;
    }

    @Override
    public User getById(Long userId) {
        return super.getById(userId);
    }

    @Override
    public User getByUsername(String username) {
        return getOne(new QueryWrapper<User>().eq("username", username).eq("deleted", 0));
    }

    @Override
    public User getByStudentNo(String studentNo) {
        return getOne(new QueryWrapper<User>().eq("student_no", studentNo).eq("deleted", 0));
    }

    @Override
    public IPage<User> pageUsers(Page<User> page, String username, String role) {
        return baseMapper.selectPageUsers(page, username, role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(User user) {
        user.setUpdateTime(LocalDateTime.now());
        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        // 加密新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disableUser(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setStatus(0); // 禁用
        user.setUpdateTime(LocalDateTime.now());
        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enableUser(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setStatus(1); // 启用
        user.setUpdateTime(LocalDateTime.now());
        return updateById(user);
    }
}