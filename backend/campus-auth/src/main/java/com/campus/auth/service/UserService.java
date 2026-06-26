package com.campus.auth.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.auth.entity.User;

public interface UserService {

    /**
     * 用户注册
     */
    boolean register(String username, String password, String realName, String studentNo, String email);

    /**
     * 用户登录
     */
    User login(String username, String password);

    /**
     * 根据ID查询用户
     */
    User getById(Long userId);

    /**
     * 根据用户名查询用户
     */
    User getByUsername(String username);

    /**
     * 根据学号查询用户
     */
    User getByStudentNo(String studentNo);

    /**
     * 分页查询用户
     */
    IPage<User> pageUsers(Page<User> page, String username, String role);

    /**
     * 更新用户信息
     */
    boolean updateUser(User user);

    /**
     * 修改密码
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 禁用用户
     */
    boolean disableUser(Long userId);

    /**
     * 启用用户
     */
    boolean enableUser(Long userId);
}