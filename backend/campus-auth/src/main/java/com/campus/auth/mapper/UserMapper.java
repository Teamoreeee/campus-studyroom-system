package com.campus.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.auth.entity.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     */
    User selectByUsername(@Param("username") String username);

    /**
     * 根据学号查询用户
     */
    User selectByStudentNo(@Param("studentNo") String studentNo);

    /**
     * 分页查询用户
     */
    IPage<User> selectPageUsers(Page<User> page, @Param("username") String username, @Param("role") String role);
}