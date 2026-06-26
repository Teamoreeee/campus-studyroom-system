package com.campus.reservation.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.reservation.entity.ReservationHistory;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationHistoryService {

    /**
     * 创建预约历史记录
     */
    boolean createHistory(ReservationHistory history);

    /**
     * 批量创建历史记录
     */
    boolean createHistoryBatch(List<ReservationHistory> histories);

    /**
     * 根据预约ID获取历史记录
     */
    List<ReservationHistory> getHistoryByReservationId(Long reservationId);

    /**
     * 分页查询历史记录
     */
    IPage<ReservationHistory> pageHistory(Page<ReservationHistory> page, Long reservationId, String action, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取用户预约历史
     */
    List<ReservationHistory> getUserReservationHistory(Long userId, Integer days);

    /**
     * 获取房间预约历史
     */
    List<ReservationHistory> getRoomReservationHistory(Long roomId, Integer days);

    /**
     * 记录状态变更
     */
    boolean recordStatusChange(Long reservationId, String action, Long actionUser, String notes);
}