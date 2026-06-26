package com.campus.reservation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.reservation.entity.Reservation;
import com.campus.reservation.mapper.ReservationMapper;
import com.campus.reservation.service.ReservationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@Transactional
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements ReservationService {

    @Override
    public boolean createReservation(Reservation reservation) {
        // 检查同一用户同一时段是否已有有效预约
        LambdaQueryWrapper<Reservation> userTimeWrapper = new LambdaQueryWrapper<>();
        userTimeWrapper.eq(Reservation::getUserId, reservation.getUserId())
                .eq(Reservation::getReserveDate, reservation.getReserveDate())
                .eq(Reservation::getStartTime, reservation.getStartTime())
                .eq(Reservation::getEndTime, reservation.getEndTime())
                .in(Reservation::getStatus, "pending", "confirmed")
                .eq(Reservation::getDeleted, 0);
        if (count(userTimeWrapper) > 0) {
            throw new RuntimeException("您在该时段已有预约，同一时段只能预约一个座位");
        }

        // 检查时间冲突
        if (checkTimeConflict(reservation.getRoomId(), reservation.getSeatId(), reservation.getReserveDate(),
                reservation.getStartTime(), reservation.getEndTime(), null)) {
            throw new RuntimeException("该时间段已被预约，请选择其他时间");
        }

        reservation.setStatus("pending");
        return save(reservation);
    }

    @Override
    public boolean createReservationBatch(List<Reservation> reservations) {
        return saveBatch(reservations);
    }

    @Override
    public boolean updateReservation(Reservation reservation) {
        return updateById(reservation);
    }

    @Override
    public boolean deleteReservation(Long reservationId) {
        return removeById(reservationId);
    }

    @Override
    public Reservation getReservationById(Long reservationId) {
        return getById(reservationId);
    }

    @Override
    public List<Reservation> getReservationsByUserId(Long userId) {
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getUserId, userId)
                .eq(Reservation::getDeleted, 0)
                .orderByDesc(Reservation::getCreateTime);

        return list(wrapper);
    }

    @Override
    public List<Reservation> getReservationsByRoomId(Long roomId) {
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getRoomId, roomId)
                .eq(Reservation::getDeleted, 0)
                .orderByDesc(Reservation::getCreateTime);

        return list(wrapper);
    }

    @Override
    public List<Reservation> getReservationsBySeatId(Long seatId) {
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getSeatId, seatId)
                .eq(Reservation::getDeleted, 0)
                .orderByDesc(Reservation::getCreateTime);

        return list(wrapper);
    }

    @Override
    public IPage<Reservation> pageReservations(Page<Reservation> page, Long userId, Long roomId, String status, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();

        if (Objects.nonNull(userId)) {
            wrapper.eq(Reservation::getUserId, userId);
        }

        if (Objects.nonNull(roomId)) {
            wrapper.eq(Reservation::getRoomId, roomId);
        }

        if (Objects.nonNull(status)) {
            wrapper.eq(Reservation::getStatus, status);
        }

        if (Objects.nonNull(startTime)) {
            wrapper.ge(Reservation::getStartTime, startTime);
        }

        if (Objects.nonNull(endTime)) {
            wrapper.le(Reservation::getEndTime, endTime);
        }

        wrapper.eq(Reservation::getDeleted, 0)
                .orderByDesc(Reservation::getCreateTime);

        return page(page, wrapper);
    }

    @Override
    public boolean confirmReservation(Long reservationId) {
        Reservation reservation = getById(reservationId);
        if (reservation != null && "pending".equals(reservation.getStatus())) {
            reservation.setStatus("confirmed");
            return updateById(reservation);
        }
        return false;
    }

    @Override
    public boolean cancelReservation(Long reservationId, String reason) {
        Reservation reservation = getById(reservationId);
        if (reservation != null && !"cancelled".equals(reservation.getStatus())) {
            reservation.setStatus("cancelled");
            reservation.setNotes(reason);
            return updateById(reservation);
        }
        return false;
    }

    @Override
    public boolean completeReservation(Long reservationId) {
        Reservation reservation = getById(reservationId);
        if (reservation != null && "confirmed".equals(reservation.getStatus())) {
            reservation.setStatus("completed");
            return updateById(reservation);
        }
        return false;
    }

    @Override
    public boolean timeoutCancelReservation(Long reservationId) {
        Reservation reservation = getById(reservationId);
        if (reservation != null) {
            reservation.setStatus("timeout");
            return updateById(reservation);
        }
        return false;
    }

    @Override
    public boolean checkTimeConflict(Long roomId, Long seatId, LocalDate reserveDate, LocalDateTime startTime, LocalDateTime endTime, Long excludeReservationId) {
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getRoomId, roomId)
                .eq(Reservation::getSeatId, seatId)
                .eq(Reservation::getReserveDate, reserveDate)
                .lt(Reservation::getStartTime, endTime)
                .gt(Reservation::getEndTime, startTime)
                .in(Reservation::getStatus, "pending", "confirmed")
                .eq(Reservation::getDeleted, 0);

        if (Objects.nonNull(excludeReservationId)) {
            wrapper.ne(Reservation::getReservationId, excludeReservationId);
        }

        return count(wrapper) > 0;
    }

    @Override
    public List<Reservation> getReservationsByTimeRange(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getRoomId, roomId)
                .lt(Reservation::getStartTime, endTime)
                .gt(Reservation::getEndTime, startTime)
                .eq(Reservation::getDeleted, 0)
                .orderByDesc(Reservation::getStartTime);

        return list(wrapper);
    }

    @Override
    public List<Reservation> getSeatReservationHistory(Long seatId, Integer days) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);

        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getSeatId, seatId)
                .ge(Reservation::getStartTime, startTime)
                .le(Reservation::getStartTime, endTime)
                .eq(Reservation::getDeleted, 0)
                .orderByDesc(Reservation::getStartTime);

        return list(wrapper);
    }

    @Override
    public String generateQRCode(Long reservationId) {
        // 生成二维码逻辑，这里返回一个示例二维码
        String qrcode = "QR_" + System.currentTimeMillis() + "_" + reservationId;
        Reservation reservation = getById(reservationId);
        if (reservation != null) {
            reservation.setQrcode(qrcode);
            updateById(reservation);
        }
        return qrcode;
    }

    @Override
    public Reservation verifyQRCode(String qrcode) {
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getQrcode, qrcode)
                .in(Reservation::getStatus, "pending", "confirmed")
                .eq(Reservation::getDeleted, 0);

        return getOne(wrapper);
    }

    @Override
    public Reservation getUserActiveReservation(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getUserId, userId)
                .in(Reservation::getStatus, "pending", "confirmed")
                .le(Reservation::getStartTime, now)
                .ge(Reservation::getEndTime, now)
                .eq(Reservation::getDeleted, 0);

        return getOne(wrapper);
    }

    @Override
    public List<Reservation> getRoomDailyStatistics(Long roomId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getRoomId, roomId)
                .ge(Reservation::getStartTime, start)
                .le(Reservation::getStartTime, end)
                .eq(Reservation::getDeleted, 0)
                .orderByDesc(Reservation::getStartTime);

        return list(wrapper);
    }

    @Override
    public List<Reservation> getSeatUsageStatistics(Long seatId, Integer days) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);

        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Reservation::getSeatId, seatId)
                .ge(Reservation::getStartTime, startTime)
                .le(Reservation::getStartTime, endTime)
                .eq(Reservation::getDeleted, 0)
                .orderByDesc(Reservation::getStartTime);

        return list(wrapper);
    }

    @Override
    public int clearUserExpiredReservations(Long userId) {
        // 使用自定义 SQL 执行物理删除，绕过 @TableLogic 逻辑删除
        return baseMapper.physicalDeleteExpiredByUserId(userId);
    }
}