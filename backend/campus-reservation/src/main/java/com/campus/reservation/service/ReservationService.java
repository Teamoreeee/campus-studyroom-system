package com.campus.reservation.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.reservation.entity.Reservation;

import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ReservationService extends IService<Reservation> {

    /**
     * 创建预约
     */
    boolean createReservation(Reservation reservation);

    /**
     * 批量创建预约
     */
    boolean createReservationBatch(List<Reservation> reservations);

    /**
     * 更新预约信息
     */
    boolean updateReservation(Reservation reservation);

    /**
     * 删除预约
     */
    boolean deleteReservation(Long reservationId);

    /**
     * 获取预约详情
     */
    Reservation getReservationById(Long reservationId);

    /**
     * 根据用户ID获取预约列表
     */
    List<Reservation> getReservationsByUserId(Long userId);

    /**
     * 根据房间ID获取预约列表
     */
    List<Reservation> getReservationsByRoomId(Long roomId);

    /**
     * 根据座位ID获取预约列表
     */
    List<Reservation> getReservationsBySeatId(Long seatId);

    /**
     * 分页查询预约
     */
    IPage<Reservation> pageReservations(Page<Reservation> page, Long userId, Long roomId, String status, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 确认预约
     */
    boolean confirmReservation(Long reservationId);

    /**
     * 取消预约
     */
    boolean cancelReservation(Long reservationId, String reason);

    /**
     * 完成预约
     */
    boolean completeReservation(Long reservationId);

    /**
     * 超时取消预约
     */
    boolean timeoutCancelReservation(Long reservationId);

    /**
     * 检查时间段是否有冲突
     */
    boolean checkTimeConflict(Long roomId, Long seatId, LocalDate reserveDate, LocalDateTime startTime, LocalDateTime endTime, Long excludeReservationId);

    /**
     * 获取指定时间段内的预约
     */
    List<Reservation> getReservationsByTimeRange(Long roomId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取座位预约历史
     */
    List<Reservation> getSeatReservationHistory(Long seatId, Integer days);

    /**
     * 生成预约二维码
     */
    String generateQRCode(Long reservationId);

    /**
     * 根据二维码验证预约
     */
    Reservation verifyQRCode(String qrcode);

    /**
     * 获取用户当前有效的预约
     */
    Reservation getUserActiveReservation(Long userId);

    /**
     * 获取房间今日预约统计
     */
    List<Reservation> getRoomDailyStatistics(Long roomId, LocalDate date);

    /**
     * 获取座位使用率统计
     */
    List<Reservation> getSeatUsageStatistics(Long seatId, Integer days);

    /**
     * 物理删除当前用户的已过期/已取消预约
     */
    int clearUserExpiredReservations(Long userId);
}