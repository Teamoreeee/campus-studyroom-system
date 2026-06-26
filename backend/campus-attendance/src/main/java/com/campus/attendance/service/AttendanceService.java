package com.campus.attendance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.attendance.entity.Attendance;

import java.time.LocalDateTime;
import java.util.List;

public interface AttendanceService {

    /**
     * 签到
     */
    boolean checkIn(Long userId, Long reservationId, Long roomId, Long seatId, String method, String location, String deviceInfo);

    /**
     * 签退（根据 reservationId）
     */
    boolean checkOutByReservation(Long userId, Long reservationId, String method, String location, String deviceInfo);

    /**
     * 延长学习时间
     */
    boolean extendTime(Long attendanceId, Integer extendMinutes, Long userId);

    /**
     * 获取当前考勤记录
     */
    Attendance getCurrentAttendance(Long userId);

    /**
     * 获取考勤详情
     */
    Attendance getAttendanceById(Long attendanceId);

    /**
     * 根据用户ID获取考勤记录
     */
    List<Attendance> getAttendanceByUserId(Long userId);

    /**
     * 根据房间ID获取考勤记录
     */
    List<Attendance> getAttendanceByRoomId(Long roomId);

    /**
     * 根据座位ID获取考勤记录
     */
    List<Attendance> getAttendanceBySeatId(Long seatId);

    /**
     * 分页查询考勤记录
     */
    IPage<Attendance> pageAttendance(Page<Attendance> page, Long userId, Long roomId, Long seatId, String status, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取用户考勤统计
     */
    Attendance getUserAttendanceStats(Long userId, Integer days);

    /**
     * 获取房间考勤统计
     */
    Attendance getRoomAttendanceStats(Long roomId, Integer days);

    /**
     * 获取座位使用统计
     */
    Attendance getSeatUsageStats(Long seatId, Integer days);

    /**
     * 超时处理
     */
    boolean handleTimeout();

    /**
     * 自动签退
     */
    boolean autoCheckOut();
}