package com.campus.reservation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.reservation.entity.Reservation;
import com.campus.reservation.entity.ReservationHistory;
import com.campus.reservation.mapper.ReservationHistoryMapper;
import com.campus.reservation.mapper.ReservationMapper;
import com.campus.reservation.service.ReservationHistoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class ReservationHistoryServiceImpl extends ServiceImpl<ReservationHistoryMapper, ReservationHistory> implements ReservationHistoryService {

    @Autowired
    private ReservationMapper reservationMapper;

    @Override
    public boolean createHistory(ReservationHistory history) {
        return save(history);
    }

    @Override
    public boolean createHistoryBatch(List<ReservationHistory> histories) {
        return saveBatch(histories);
    }

    @Override
    public List<ReservationHistory> getHistoryByReservationId(Long reservationId) {
        LambdaQueryWrapper<ReservationHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReservationHistory::getReservationId, reservationId)
                .orderByDesc(ReservationHistory::getActionTime);

        return list(wrapper);
    }

    @Override
    public IPage<ReservationHistory> pageHistory(Page<ReservationHistory> page, Long reservationId, String action, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<ReservationHistory> wrapper = new LambdaQueryWrapper<>();

        if (Objects.nonNull(reservationId)) {
            wrapper.eq(ReservationHistory::getReservationId, reservationId);
        }

        if (Objects.nonNull(action)) {
            wrapper.eq(ReservationHistory::getAction, action);
        }

        if (Objects.nonNull(startTime)) {
            wrapper.ge(ReservationHistory::getActionTime, startTime);
        }

        if (Objects.nonNull(endTime)) {
            wrapper.le(ReservationHistory::getActionTime, endTime);
        }

        wrapper.orderByDesc(ReservationHistory::getActionTime);

        return page(page, wrapper);
    }

    @Override
    public List<ReservationHistory> getUserReservationHistory(Long userId, Integer days) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);

        LambdaQueryWrapper<ReservationHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReservationHistory::getUserId, userId)
                .ge(ReservationHistory::getActionTime, startTime)
                .le(ReservationHistory::getActionTime, endTime)
                .orderByDesc(ReservationHistory::getActionTime);

        return list(wrapper);
    }

    @Override
    public List<ReservationHistory> getRoomReservationHistory(Long roomId, Integer days) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);

        LambdaQueryWrapper<ReservationHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReservationHistory::getRoomId, roomId)
                .ge(ReservationHistory::getActionTime, startTime)
                .le(ReservationHistory::getActionTime, endTime)
                .orderByDesc(ReservationHistory::getActionTime);

        return list(wrapper);
    }

    @Override
    public boolean recordStatusChange(Long reservationId, String action, Long actionUser, String notes) {
        Reservation reservation = reservationMapper.selectById(reservationId);
        if (reservation != null) {
            ReservationHistory history = new ReservationHistory();
            history.setReservationId(reservationId);
            history.setUserId(reservation.getUserId());
            history.setRoomId(reservation.getRoomId());
            history.setSeatId(reservation.getSeatId());
            history.setReservationDate(reservation.getReservationDate());
            history.setStartTime(reservation.getStartTime());
            history.setEndTime(reservation.getEndTime());
            history.setStatus(reservation.getStatus());
            history.setPurpose(reservation.getPurpose());
            history.setNotes(reservation.getNotes());
            history.setQrcode(reservation.getQrcode());
            history.setAction(action);
            history.setActionUser(actionUser);
            history.setNotes(notes);

            return save(history);
        }
        return false;
    }
}