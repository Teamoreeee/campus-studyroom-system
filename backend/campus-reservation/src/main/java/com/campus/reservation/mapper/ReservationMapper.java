package com.campus.reservation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.reservation.entity.Reservation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Delete;

@Mapper
public interface ReservationMapper extends BaseMapper<Reservation> {

    /**
     * 物理删除当前用户的已过期/已取消预约
     */
    @Delete("DELETE FROM reservation WHERE user_id = #{userId} " +
            "AND (status IN ('cancelled', 'timeout') " +
            "OR (status IN ('pending', 'confirmed') " +
            "    AND (reserve_date < CURDATE() OR (reserve_date = CURDATE() AND end_time < CURTIME()))))")
    int physicalDeleteExpiredByUserId(@Param("userId") Long userId);
}