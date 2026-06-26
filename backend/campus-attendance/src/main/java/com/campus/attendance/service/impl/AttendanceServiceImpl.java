package com.campus.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.attendance.entity.Attendance;
import com.campus.attendance.entity.AttendanceRecord;
import com.campus.attendance.entity.AttendanceRule;
import com.campus.attendance.mapper.AttendanceMapper;
import com.campus.attendance.service.AttendanceRecordService;
import com.campus.attendance.service.AttendanceRuleService;
import com.campus.attendance.service.AttendanceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@Transactional
public class AttendanceServiceImpl extends ServiceImpl<AttendanceMapper, Attendance> implements AttendanceService {

    @Autowired
    private AttendanceRecordService attendanceRecordService;

    @Autowired
    private AttendanceRuleService attendanceRuleService;

    @Override
    public boolean checkIn(Long userId, Long reservationId, Long roomId, Long seatId, String method, String location, String deviceInfo) {
        // 检查该预约是否已签到
        LambdaQueryWrapper<Attendance> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.eq(Attendance::getUserId, userId)
                .eq(Attendance::getReservationId, reservationId)
                .eq(Attendance::getStatus, "active")
                .eq(Attendance::getDeleted, 0);

        if (count(activeWrapper) > 0) {
            throw new RuntimeException("该预约已签到，请先签退");
        }

        // 获取考勤规则（没有规则时默认允许签到，与 isTimeAllowed 保持一致）
        AttendanceRule rule = attendanceRuleService.getRuleByRoomId(roomId);
        if (rule != null && !"active".equals(rule.getStatus())) {
            throw new RuntimeException("该房间当前不允许签到");
        }

        // 检查时间是否允许（有规则时才校验）
        if (rule != null && !attendanceRuleService.isTimeAllowed(roomId, LocalTime.now())) {
            throw new RuntimeException("当前时间不在允许签到的时间范围内");
        }

        // 创建考勤记录
        Attendance attendance = new Attendance();
        attendance.setUserId(userId);
        attendance.setReservationId(reservationId);
        attendance.setRoomId(roomId);
        attendance.setSeatId(seatId);
        attendance.setCheckInTime(LocalDateTime.now());
        attendance.setStatus("active");
        attendance.setCheckInMethod(method);
        attendance.setLocation(location);
        attendance.setDeviceInfo(deviceInfo);

        // 保存考勤记录
        boolean success = save(attendance);
        if (success) {
            // 记录签到记录
            AttendanceRecord record = new AttendanceRecord();
            record.setAttendanceId(attendance.getAttendanceId());
            record.setUserId(userId);
            record.setRoomId(roomId);
            record.setSeatId(seatId);
            record.setAction("check_in");
            record.setActionTime(LocalDateTime.now());
            record.setLocation(location);
            record.setDeviceInfo(deviceInfo);

            attendanceRecordService.createRecord(record);
        }

        return success;
    }

    @Override
    public boolean checkOutByReservation(Long userId, Long reservationId, String method, String location, String deviceInfo) {
        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Attendance::getUserId, userId)
                .eq(Attendance::getReservationId, reservationId)
                .eq(Attendance::getStatus, "active")
                .eq(Attendance::getDeleted, 0)
                .orderByDesc(Attendance::getCreateTime)
                .last("LIMIT 1");

        Attendance attendance = getOne(wrapper);
        if (attendance == null) {
            throw new RuntimeException("没有进行中的考勤记录");
        }

        // 设置签退时间
        attendance.setCheckOutTime(LocalDateTime.now());

        // 计算学习时长
        if (attendance.getCheckInTime() != null) {
            long durationMinutes = java.time.Duration.between(attendance.getCheckInTime(), LocalDateTime.now()).toMinutes();
            attendance.setDurationMinutes((int) durationMinutes);
        }

        attendance.setStatus("completed");
        attendance.setCheckOutMethod(method);

        // 更新考勤记录
        boolean success = updateById(attendance);
        if (success) {
            // 记录签退记录
            AttendanceRecord record = new AttendanceRecord();
            record.setAttendanceId(attendance.getAttendanceId());
            record.setUserId(userId);
            record.setRoomId(attendance.getRoomId());
            record.setSeatId(attendance.getSeatId());
            record.setAction("check_out");
            record.setActionTime(LocalDateTime.now());
            record.setLocation(location);
            record.setDeviceInfo(deviceInfo);

            attendanceRecordService.createRecord(record);
        }

        return success;
    }

    @Override
    public boolean extendTime(Long attendanceId, Integer extendMinutes, Long userId) {
        Attendance attendance = getById(attendanceId);
        if (attendance == null || !"active".equals(attendance.getStatus())) {
            throw new RuntimeException("考勤记录不存在或已过期");
        }

        AttendanceRule rule = attendanceRuleService.getRuleByRoomId(attendance.getRoomId());
        if (rule == null || !rule.getAllowExtend()) {
            throw new RuntimeException("该房间不允许延长学习时间");
        }

        // 检查延长次数和时间
        if (!attendanceRuleService.canExtendTime(attendance.getRoomId(),
                attendanceRuleService.countExtendTimes(attendanceId), extendMinutes)) {
            throw new RuntimeException("超出延长学习时间限制");
        }

        // 记录延长记录
        AttendanceRecord record = new AttendanceRecord();
        record.setAttendanceId(attendanceId);
        record.setUserId(userId);
        record.setRoomId(attendance.getRoomId());
        record.setSeatId(attendance.getSeatId());
        record.setAction("extend");
        record.setActionTime(LocalDateTime.now());
        record.setNotes("延长学习时间：" + extendMinutes + "分钟");

        return attendanceRecordService.createRecord(record);
    }

    @Override
    public Attendance getCurrentAttendance(Long userId) {
        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Attendance::getUserId, userId)
                .eq(Attendance::getStatus, "active")
                .eq(Attendance::getDeleted, 0)
                .orderByDesc(Attendance::getCreateTime)
                .last("LIMIT 1");

        return getOne(wrapper);
    }

    @Override
    public Attendance getAttendanceById(Long attendanceId) {
        return getById(attendanceId);
    }

    @Override
    public List<Attendance> getAttendanceByUserId(Long userId) {
        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Attendance::getUserId, userId)
                .eq(Attendance::getDeleted, 0)
                .orderByDesc(Attendance::getCreateTime);

        return list(wrapper);
    }

    @Override
    public List<Attendance> getAttendanceByRoomId(Long roomId) {
        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Attendance::getRoomId, roomId)
                .eq(Attendance::getDeleted, 0)
                .orderByDesc(Attendance::getCreateTime);

        return list(wrapper);
    }

    @Override
    public List<Attendance> getAttendanceBySeatId(Long seatId) {
        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Attendance::getSeatId, seatId)
                .eq(Attendance::getDeleted, 0)
                .orderByDesc(Attendance::getCreateTime);

        return list(wrapper);
    }

    @Override
    public IPage<Attendance> pageAttendance(Page<Attendance> page, Long userId, Long roomId, Long seatId, String status, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();

        if (Objects.nonNull(userId)) {
            wrapper.eq(Attendance::getUserId, userId);
        }

        if (Objects.nonNull(roomId)) {
            wrapper.eq(Attendance::getRoomId, roomId);
        }

        if (Objects.nonNull(seatId)) {
            wrapper.eq(Attendance::getSeatId, seatId);
        }

        if (Objects.nonNull(status)) {
            wrapper.eq(Attendance::getStatus, status);
        }

        if (Objects.nonNull(startTime)) {
            wrapper.ge(Attendance::getCheckInTime, startTime);
        }

        if (Objects.nonNull(endTime)) {
            wrapper.le(Attendance::getCheckOutTime, endTime);
        }

        wrapper.eq(Attendance::getDeleted, 0)
                .orderByDesc(Attendance::getCreateTime);

        return page(page, wrapper);
    }

    @Override
    public Attendance getUserAttendanceStats(Long userId, Integer days) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);

        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Attendance::getUserId, userId)
                .ge(Attendance::getCheckInTime, startTime)
                .le(Attendance::getCheckInTime, endTime)
                .eq(Attendance::getDeleted, 0);

        List<Attendance> attendances = list(wrapper);

        Attendance stats = new Attendance();
        if (!attendances.isEmpty()) {
            long totalDuration = attendances.stream()
                    .filter(a -> a.getDurationMinutes() != null)
                    .mapToLong(Attendance::getDurationMinutes)
                    .sum();
            stats.setDurationMinutes((int) totalDuration);
        }

        return stats;
    }

    @Override
    public Attendance getRoomAttendanceStats(Long roomId, Integer days) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);

        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Attendance::getRoomId, roomId)
                .ge(Attendance::getCheckInTime, startTime)
                .le(Attendance::getCheckInTime, endTime)
                .eq(Attendance::getDeleted, 0);

        List<Attendance> attendances = list(wrapper);

        Attendance stats = new Attendance();
        if (!attendances.isEmpty()) {
            long totalDuration = attendances.stream()
                    .filter(a -> a.getDurationMinutes() != null)
                    .mapToLong(Attendance::getDurationMinutes)
                    .sum();
            stats.setDurationMinutes((int) totalDuration);
            stats.setCurrentCount(attendances.size());
        }

        return stats;
    }

    @Override
    public Attendance getSeatUsageStats(Long seatId, Integer days) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);

        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Attendance::getSeatId, seatId)
                .ge(Attendance::getCheckInTime, startTime)
                .le(Attendance::getCheckInTime, endTime)
                .eq(Attendance::getDeleted, 0);

        List<Attendance> attendances = list(wrapper);

        Attendance stats = new Attendance();
        if (!attendances.isEmpty()) {
            long totalDuration = attendances.stream()
                    .filter(a -> a.getDurationMinutes() != null)
                    .mapToLong(Attendance::getDurationMinutes)
                    .sum();
            stats.setDurationMinutes((int) totalDuration);
            stats.setCurrentCount(attendances.size());
        }

        return stats;
    }

    @Override
    public boolean handleTimeout() {
        // 获取所有超时的考勤记录
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Attendance::getStatus, "active")
                .lt(Attendance::getCheckInTime, now.minusHours(4)) // 超过4小时未签退
                .eq(Attendance::getDeleted, 0);

        List<Attendance> timeoutAttendances = list(wrapper);

        for (Attendance attendance : timeoutAttendances) {
            attendance.setStatus("timeout");
            updateById(attendance);

            // 记录超时处理
            AttendanceRecord record = new AttendanceRecord();
            record.setAttendanceId(attendance.getAttendanceId());
            record.setUserId(attendance.getUserId());
            record.setRoomId(attendance.getRoomId());
            record.setSeatId(attendance.getSeatId());
            record.setAction("timeout");
            record.setActionTime(now);
            record.setNotes("超时自动签退");

            attendanceRecordService.createRecord(record);
        }

        return !timeoutAttendances.isEmpty();
    }

    @Override
    public boolean autoCheckOut() {
        // 获取需要自动签退的考勤记录
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Attendance::getStatus, "active")
                .ne(Attendance::getCheckOutTime, null)
                .lt(Attendance::getCheckOutTime, now)
                .eq(Attendance::getDeleted, 0);

        List<Attendance> autoCheckOutAttendances = list(wrapper);

        for (Attendance attendance : autoCheckOutAttendances) {
            attendance.setStatus("completed");

            // 计算实际学习时长
            if (attendance.getCheckInTime() != null) {
                long durationMinutes = java.time.Duration.between(attendance.getCheckInTime(), attendance.getCheckOutTime()).toMinutes();
                attendance.setDurationMinutes((int) durationMinutes);
            }

            updateById(attendance);

            // 记录自动签退
            AttendanceRecord record = new AttendanceRecord();
            record.setAttendanceId(attendance.getAttendanceId());
            record.setUserId(attendance.getUserId());
            record.setRoomId(attendance.getRoomId());
            record.setSeatId(attendance.getSeatId());
            record.setAction("auto_check_out");
            record.setActionTime(now);
            record.setNotes("系统自动签退");

            attendanceRecordService.createRecord(record);
        }

        return !autoCheckOutAttendances.isEmpty();
    }
}