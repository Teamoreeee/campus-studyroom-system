package com.campus.attendance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.attendance.entity.AttendanceRule;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public interface AttendanceRuleService {

    /**
     * 创建考勤规则
     */
    boolean createRule(AttendanceRule rule);

    /**
     * 更新考勤规则
     */
    boolean updateRule(AttendanceRule rule);

    /**
     * 删除考勤规则
     */
    boolean deleteRule(Long ruleId);

    /**
     * 获取考勤规则详情
     */
    AttendanceRule getRuleById(Long ruleId);

    /**
     * 获取所有考勤规则
     */
    List<AttendanceRule> getAllRules();

    /**
     * 根据房间ID获取考勤规则
     */
    AttendanceRule getRuleByRoomId(Long roomId);

    /**
     * 分页查询考勤规则
     */
    IPage<AttendanceRule> pageRules(Page<AttendanceRule> page, String ruleName, String status);

    /**
     * 启用/禁用考勤规则
     */
    boolean toggleRuleStatus(Long ruleId, String status);

    /**
     * 检查时间是否在允许范围内
     */
    boolean isTimeAllowed(Long roomId, LocalTime currentTime);

    /**
     * 计算剩余学习时间
     */
    Integer calculateRemainingTime(Long roomId, LocalDateTime startTime);

    /**
     * 检查是否可以延长
     */
    boolean canExtendTime(Long roomId, Integer extendTimes, Integer currentDuration);

    /**
     * 获取自动签退时间
     */
    LocalDateTime getAutoCheckOutTime(Long roomId, LocalDateTime checkInTime);

    /**
     * 统计延长次数
     */
    Integer countExtendTimes(Long attendanceId);
}