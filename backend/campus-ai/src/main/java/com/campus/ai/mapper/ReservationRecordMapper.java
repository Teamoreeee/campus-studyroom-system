package com.campus.ai.mapper;

import com.campus.ai.entity.ReservationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReservationRecordMapper {

    @Select("SELECT user_id, room_id, seat_id, COUNT(*) as count " +
            "FROM reservation " +
            "WHERE status IN ('pending', 'confirmed', 'checked_in') " +
            "AND deleted = 0 " +
            "GROUP BY user_id, room_id, seat_id")
    List<ReservationRecord> selectAllActiveRecords();

    @Select("SELECT user_id, room_id, seat_id, COUNT(*) as count " +
            "FROM reservation " +
            "WHERE user_id = #{userId} " +
            "AND status IN ('pending', 'confirmed', 'checked_in') " +
            "AND deleted = 0 " +
            "GROUP BY room_id, seat_id")
    List<ReservationRecord> selectByUserId(@Param("userId") Long userId);
}
