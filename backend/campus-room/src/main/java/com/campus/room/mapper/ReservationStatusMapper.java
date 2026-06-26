package com.campus.room.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReservationStatusMapper {

    /**
     * 统计指定自习室今日被有效预约占用的座位数
     */
    @Select("SELECT COUNT(DISTINCT seat_id) FROM reservation " +
            "WHERE room_id = #{roomId} AND deleted = 0 " +
            "AND status IN ('pending', 'confirmed') " +
            "AND reserve_date = CURDATE()")
    int countOccupiedSeatsToday(@Param("roomId") Long roomId);

    /**
     * 查询指定自习室指定日期指定时间段被有效预约占用的座位ID列表
     */
    @Select("SELECT DISTINCT seat_id FROM reservation " +
            "WHERE room_id = #{roomId} AND deleted = 0 " +
            "AND status IN ('pending', 'confirmed') " +
            "AND reserve_date = #{date} " +
            "AND start_time < #{endTime} " +
            "AND end_time > #{startTime}")
    List<Long> findOccupiedSeatIds(@Param("roomId") Long roomId, @Param("date") String date,
                                      @Param("startTime") String startTime, @Param("endTime") String endTime);
}
