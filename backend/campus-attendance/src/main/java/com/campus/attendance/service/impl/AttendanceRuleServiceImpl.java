package com.campus.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.attendance.entity.AttendanceRule;
import com.campus.attendance.mapper.AttendanceRuleMapper;
import com.campus.attendance.service.AttendanceRuleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class AttendanceRuleServiceImpl extends ServiceImpl<AttendanceRuleMapper, AttendanceRule> implements AttendanceRuleService {

    @Override
    public boolean createRule(AttendanceRule rule) {
        if (rule.getStatus() == null) {
            rule.setStatus("active");
        }
        if (rule.getCreateTime() == null) {
            rule.setCreateTime(LocalDateTime.now());
        }
        if (rule.getUpdateTime() == null) {
            rule.setUpdateTime(LocalDateTime.now());
        }
        return save(rule);
    }

    @Override
    public boolean updateRule(AttendanceRule rule) {
        rule.setUpdateTime(LocalDateTime.now());
        return updateById(rule);
    }

    @Override
    public boolean deleteRule(Long ruleId) {
        return removeById(ruleId);
    }

    @Override
    public AttendanceRule getRuleById(Long ruleId) {
        return getById(ruleId);
    }

    @Override
    public List<AttendanceRule> getAllRules() {
        LambdaQueryWrapper<AttendanceRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AttendanceRule::getDeleted, 0)
                .orderByDesc(AttendanceRule::getCreateTime);
        return list(wrapper);
    }

    @Override
    public AttendanceRule getRuleByRoomId(Long roomId) {
        LambdaQueryWrapper<AttendanceRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AttendanceRule::getRoomId, roomId)
                .eq(AttendanceRule::getStatus, "active")
                .eq(AttendanceRule::getDeleted, 0)
                .orderByDesc(AttendanceRule::getCreateTime)
                .last("LIMIT 1");
        return getOne(wrapper);
    }

    @Override
    public IPage<AttendanceRule> pageRules(Page<AttendanceRule> page, String ruleName, String status) {
        LambdaQueryWrapper<AttendanceRule> wrapper = new LambdaQueryWrapper<>();
        if (Objects.nonNull(ruleName) && !ruleName.isEmpty()) {
            wrapper.like(AttendanceRule::getRuleName, ruleName);
        }
        if (Objects.nonNull(status) && !status.isEmpty()) {
            wrapper.eq(AttendanceRule::getStatus, status);
        }
        wrapper.eq(AttendanceRule::getDeleted, 0)
                .orderByDesc(AttendanceRule::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public boolean toggleRuleStatus(Long ruleId, String status) {
        AttendanceRule rule = getById(ruleId);
        if (rule == null) {
            return false;
        }
        rule.setStatus(status);
        rule.setUpdateTime(LocalDateTime.now());
        return updateById(rule);
    }

    @Override
    public boolean isTimeAllowed(Long roomId, LocalTime currentTime) {
        AttendanceRule rule = getRuleByRoomId(roomId);
        if (rule == null || !"active".equals(rule.getStatus())) {
            return true; // 没有规则默认允许
        }
        LocalTime start = rule.getStartTime();
        LocalTime end = rule.getEndTime();
        if (start == null || end == null) {
            return true;
        }
        return !currentTime.isBefore(start) && !currentTime.isAfter(end);
    }

    @Override
    public Integer calculateRemainingTime(Long roomId, LocalDateTime startTime) {
        AttendanceRule rule = getRuleByRoomId(roomId);
        if (rule == null || rule.getMaxDuration() == null) {
            return null;
        }
        int elapsed = (int) java.time.Duration.between(startTime, LocalDateTime.now()).toMinutes();
        return Math.max(0, rule.getMaxDuration() - elapsed);
    }

    @Override
    public boolean canExtendTime(Long roomId, Integer extendTimes, Integer currentDuration) {
        AttendanceRule rule = getRuleByRoomId(roomId);
        if (rule == null || !Boolean.TRUE.equals(rule.getAllowExtend())) {
            return false;
        }
        if (rule.getMaxExtendTimes() != null && extendTimes >= rule.getMaxExtendTimes()) {
            return false;
        }
        if (rule.getMaxExtendDuration() != null && currentDuration >= rule.getMaxExtendDuration()) {
            return false;
        }
        return true;
    }

    @Override
    public LocalDateTime getAutoCheckOutTime(Long roomId, LocalDateTime checkInTime) {
        AttendanceRule rule = getRuleByRoomId(roomId);
        if (rule == null || !Boolean.TRUE.equals(rule.getAutoCheckOut()) || rule.getCheckOutDelay() == null) {
            return null;
        }
        return checkInTime.plusMinutes(rule.getCheckOutDelay());
    }

    @Override
    public Integer countExtendTimes(Long attendanceId) {
        // 简化实现：从考勤记录中统计 extend 次数
        // 实际项目中可能需要单独的 extend 记录表
        return 0;
    }
}
