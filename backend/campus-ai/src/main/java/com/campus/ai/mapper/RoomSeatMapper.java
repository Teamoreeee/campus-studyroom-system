package com.campus.ai.mapper;

import com.campus.ai.entity.RoomInfo;
import com.campus.ai.entity.SeatInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoomSeatMapper {

    @Select("SELECT room_id, room_name, building, capacity FROM study_room " +
            "WHERE deleted = 0")
    List<RoomInfo> selectAllRooms();

    @Select("SELECT room_id, room_name, building, capacity FROM study_room " +
            "WHERE building = #{building} AND deleted = 0")
    List<RoomInfo> selectRoomsByBuilding(@Param("building") String building);

    @Select("SELECT room_id, room_name, building, capacity FROM study_room " +
            "WHERE room_id = #{roomId} AND deleted = 0")
    RoomInfo selectRoomById(@Param("roomId") Long roomId);

    @Select("SELECT seat_id, room_id, seat_number, type, has_power " +
            "FROM study_seat " +
            "WHERE room_id = #{roomId} AND status = 'available' AND deleted = 0 " +
            "ORDER BY seat_id LIMIT 10")
    List<SeatInfo> selectAvailableSeatsByRoom(@Param("roomId") Long roomId);

    @Select("SELECT seat_id, room_id, seat_number, type, has_power " +
            "FROM study_seat " +
            "WHERE room_id = #{roomId} AND type = #{type} AND status = 'available' AND deleted = 0 " +
            "LIMIT 5")
    List<SeatInfo> selectAvailableSeatsByType(@Param("roomId") Long roomId, @Param("type") String type);

    @Select("SELECT seat_id, room_id, seat_number, type, has_power " +
            "FROM study_seat " +
            "WHERE room_id = #{roomId} AND type = #{type} AND has_power = #{hasPower} " +
            "AND status = 'available' AND deleted = 0 " +
            "LIMIT 5")
    List<SeatInfo> selectAvailableSeatsByTypes(@Param("roomId") Long roomId, @Param("type") String type, @Param("hasPower") Boolean hasPower);
}
