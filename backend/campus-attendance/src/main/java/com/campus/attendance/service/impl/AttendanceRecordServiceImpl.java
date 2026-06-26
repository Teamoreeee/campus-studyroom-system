package com.campus.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.attendance.entity.AttendanceRecord;
import com.campus.attendance.mapper.AttendanceRecordMapper;
import com.campus.attendance.service.AttendanceRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class AttendanceRecordServiceImpl extends ServiceImpl<AttendanceRecordMapper, AttendanceRecord> implements AttendanceRecordService {

    @Override
    public boolean createRecord(AttendanceRecord record) {
        if (record.getCreateTime() == null) {
            record.setCreateTime(LocalDateTime.now());
        }
        if (record.getActionTime() == null) {
            record.setActionTime(LocalDateTime.now());
        }
        return save(record);
    }

    @Override
    public boolean createRecordBatch(List<AttendanceRecord> records) {
        records.forEach(r -> {
            if (r.getCreateTime() == null) {
                r.setCreateTime(LocalDateTime.now());
            }
            if (r.getActionTime() == null) {
                r.setActionTime(LocalDateTime.now());
            }
        });
        return saveBatch(records);
    }

    @Override
    public List<AttendanceRecord> getRecordsByAttendanceId(Long attendanceId) {
        LambdaQueryWrapper<AttendanceRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AttendanceRecord::getAttendanceId, attendanceId)
                .orderByDesc(AttendanceRecord::getActionTime);
        return list(wrapper);
    }

    @Override
    public List<AttendanceRecord> getRecordsByUserId(Long userId) {
        LambdaQueryWrapper<AttendanceRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AttendanceRecord::getUserId, userId)
                .orderByDesc(AttendanceRecord::getActionTime);
        return list(wrapper);
    }

    @Override
    public IPage<AttendanceRecord> pageRecords(Page<AttendanceRecord> page, Long attendanceId, Long userId,
                                                  String action, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<AttendanceRecord> wrapper = new LambdaQueryWrapper<>();
        if (Objects.nonNull(attendanceId)) {
            wrapper.eq(AttendanceRecord::getAttendanceId, attendanceId);
        }
        if (Objects.nonNull(userId)) {
            wrapper.eq(AttendanceRecord::getUserId, userId);
        }
        if (Objects.nonNull(action)) {
            wrapper.eq(AttendanceRecord::getAction, action);
        }
        if (Objects.nonNull(startTime)) {
            wrapper.ge(AttendanceRecord::getActionTime, startTime);
        }
        if (Objects.nonNull(endTime)) {
            wrapper.le(AttendanceRecord::getActionTime, endTime);
        }
        wrapper.orderByDesc(AttendanceRecord::getActionTime);
        return page(page, wrapper);
    }

    @Override
    public boolean recordCheckIn(Long userId, Long roomId, Long seatId, Long attendanceId, String location, String deviceInfo) {
        AttendanceRecord record = new AttendanceRecord();
        record.setUserId(userId);
        record.setRoomId(roomId);
        record.setSeatId(seatId);
        record.setAttendanceId(attendanceId);
        record.setAction("check_in");
        record.setLocation(location);
        record.setDeviceInfo(deviceInfo);
        return createRecord(record);
    }

    @Override
    public boolean recordCheckOut(Long attendanceId, Long userId, String location, String deviceInfo) {
        AttendanceRecord record = new AttendanceRecord();
        record.setAttendanceId(attendanceId);
        record.setUserId(userId);
        record.setAction("check_out");
        record.setLocation(location);
        record.setDeviceInfo(deviceInfo);
        return createRecord(record);
    }

    @Override
    public boolean recordExtend(Long attendanceId, Long userId, Integer extendMinutes) {
        AttendanceRecord record = new AttendanceRecord();
        record.setAttendanceId(attendanceId);
        record.setUserId(userId);
        record.setAction("extend");
        record.setNotes("延长学习时间：" + extendMinutes + "分钟");
        return createRecord(record);
    }

    @Override
    public boolean recordTimeout(Long attendanceId) {
        AttendanceRecord record = new AttendanceRecord();
        record.setAttendanceId(attendanceId);
        record.setAction("timeout");
        record.setNotes("超时自动签退");
        return createRecord(record);
    }
}
