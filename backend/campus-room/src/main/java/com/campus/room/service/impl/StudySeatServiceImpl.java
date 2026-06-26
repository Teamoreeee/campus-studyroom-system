package com.campus.room.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.room.entity.StudySeat;
import com.campus.room.mapper.StudySeatMapper;
import com.campus.room.service.StudySeatService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class StudySeatServiceImpl extends ServiceImpl<StudySeatMapper, StudySeat> implements StudySeatService {

    @Override
    public boolean createSeat(StudySeat seat) {
        seat.setStatus("available"); // 默认可用状态
        return save(seat);
    }

    @Override
    public boolean createSeatsBatch(List<StudySeat> seats) {
        return saveBatch(seats);
    }

    @Override
    public boolean updateSeat(StudySeat seat) {
        return updateById(seat);
    }

    @Override
    public boolean deleteSeat(Long seatId) {
        return removeById(seatId);
    }

    @Override
    public StudySeat getSeatById(Long seatId) {
        return getById(seatId);
    }

    @Override
    public List<StudySeat> getSeatsByRoomId(Long roomId) {
        LambdaQueryWrapper<StudySeat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudySeat::getRoomId, roomId)
                .eq(StudySeat::getDeleted, 0)
                .orderByAsc(StudySeat::getSeatNumber);

        return list(wrapper);
    }

    @Override
    public List<StudySeat> getSeatsByZone(Long roomId, String zone) {
        LambdaQueryWrapper<StudySeat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudySeat::getRoomId, roomId)
                .eq(StudySeat::getZone, zone)
                .eq(StudySeat::getDeleted, 0)
                .orderByAsc(StudySeat::getSeatNumber);

        return list(wrapper);
    }

    @Override
    public List<StudySeat> getAvailableSeats(Long roomId) {
        LambdaQueryWrapper<StudySeat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudySeat::getRoomId, roomId)
                .eq(StudySeat::getStatus, "available")
                .eq(StudySeat::getDeleted, 0)
                .orderByAsc(StudySeat::getSeatNumber);

        return list(wrapper);
    }

    @Override
    public List<StudySeat> getSeatsByType(Long roomId, String type) {
        LambdaQueryWrapper<StudySeat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudySeat::getRoomId, roomId)
                .eq(StudySeat::getType, type)
                .eq(StudySeat::getDeleted, 0)
                .orderByAsc(StudySeat::getSeatNumber);

        return list(wrapper);
    }

    @Override
    public IPage<StudySeat> pageSeats(Page<StudySeat> page, Long roomId, String zone, String status, String type) {
        LambdaQueryWrapper<StudySeat> wrapper = new LambdaQueryWrapper<>();

        if (Objects.nonNull(roomId)) {
            wrapper.eq(StudySeat::getRoomId, roomId);
        }

        if (Objects.nonNull(zone)) {
            wrapper.eq(StudySeat::getZone, zone);
        }

        if (Objects.nonNull(status)) {
            wrapper.eq(StudySeat::getStatus, status);
        }

        if (Objects.nonNull(type)) {
            wrapper.eq(StudySeat::getType, type);
        }

        wrapper.eq(StudySeat::getDeleted, 0)
                .orderByAsc(StudySeat::getSeatNumber);

        return page(page, wrapper);
    }

    @Override
    public boolean updateSeatStatus(Long seatId, String status, Long userId, Long reservationId) {
        StudySeat seat = getById(seatId);
        if (seat != null) {
            seat.setStatus(status);
            if (Objects.nonNull(userId)) {
                seat.setCurrentUserId(userId);
            }
            if (Objects.nonNull(reservationId)) {
                seat.setCurrentReservationId(reservationId);
            }
            return updateById(seat);
        }
        return false;
    }

    @Override
    public boolean occupySeat(Long seatId, Long userId, Long reservationId) {
        return updateSeatStatus(seatId, "occupied", userId, reservationId);
    }

    @Override
    public boolean releaseSeat(Long seatId) {
        return updateSeatStatus(seatId, "available", null, null);
    }

    @Override
    public List<StudySeat> getSeatUsageStatistics(Long roomId) {
        LambdaQueryWrapper<StudySeat> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudySeat::getRoomId, roomId)
                .select(StudySeat::getStatus)
                .groupBy(StudySeat::getStatus)
                .orderByDesc(StudySeat::getStatus);

        return list(wrapper);
    }

    @Override
    public boolean isSeatAvailable(Long seatId) {
        StudySeat seat = getById(seatId);
        return seat != null && "available".equals(seat.getStatus());
    }
}