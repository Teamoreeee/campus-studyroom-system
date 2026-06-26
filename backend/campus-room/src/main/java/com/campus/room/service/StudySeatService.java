package com.campus.room.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.room.entity.StudySeat;

import java.util.List;

public interface StudySeatService {

    /**
     * 创建座位
     */
    boolean createSeat(StudySeat seat);

    /**
     * 批量创建座位
     */
    boolean createSeatsBatch(List<StudySeat> seats);

    /**
     * 更新座位信息
     */
    boolean updateSeat(StudySeat seat);

    /**
     * 删除座位
     */
    boolean deleteSeat(Long seatId);

    /**
     * 获取座位详情
     */
    StudySeat getSeatById(Long seatId);

    /**
     * 根据房间ID获取座位列表
     */
    List<StudySeat> getSeatsByRoomId(Long roomId);

    /**
     * 根据区域获取座位
     */
    List<StudySeat> getSeatsByZone(Long roomId, String zone);

    /**
     * 获取可用座位
     */
    List<StudySeat> getAvailableSeats(Long roomId);

    /**
     * 获取指定类型的座位
     */
    List<StudySeat> getSeatsByType(Long roomId, String type);

    /**
     * 分页查询座位
     */
    IPage<StudySeat> pageSeats(Page<StudySeat> page, Long roomId, String zone, String status, String type);

    /**
     * 更新座位状态
     */
    boolean updateSeatStatus(Long seatId, String status, Long userId, Long reservationId);

    /**
     * 占用座位
     */
    boolean occupySeat(Long seatId, Long userId, Long reservationId);

    /**
     * 释放座位
     */
    boolean releaseSeat(Long seatId);

    /**
     * 获取座位使用统计
     */
    List<StudySeat> getSeatUsageStatistics(Long roomId);

    /**
     * 检查座位是否可用
     */
    boolean isSeatAvailable(Long seatId);
}