package com.campus.attendance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.attendance.entity.AttendanceRecord;

import java.time.LocalDateTime;
import java.util.List;

public interface AttendanceRecordService {

    /**
     * 创建考勤记录
     */
    boolean createRecord(AttendanceRecord record);

    /**
     * 批量创建考勤记录
     */
    boolean createRecordBatch(List<AttendanceRecord> records);

    /**
     * 根据考勤ID获取记录
     */
    List<AttendanceRecord> getRecordsByAttendanceId(Long attendanceId);

    /**
     * 根据用户ID获取记录
     */
    List<AttendanceRecord> getRecordsByUserId(Long userId);

    /**
     * 分页查询考勤记录
     */
    IPage<AttendanceRecord> pageRecords(Page<AttendanceRecord> page, Long attendanceId, Long userId, String action, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 记录签到
     */
    boolean recordCheckIn(Long userId, Long roomId, Long seatId, Long attendanceId, String location, String deviceInfo);

    /**
     * 记录签退
     */
    boolean recordCheckOut(Long attendanceId, Long userId, String location, String deviceInfo);

    /**
     * 记录延长学习时间
     */
    boolean recordExtend(Long attendanceId, Long userId, Integer extendMinutes);

    /**
     * 记录超时处理
     */
    boolean recordTimeout(Long attendanceId);
}