package com.campus.room.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.room.entity.StudyRoom;

import java.util.List;

public interface StudyRoomService {

    /**
     * 创建自习室
     */
    boolean createRoom(StudyRoom room);

    /**
     * 更新自习室信息
     */
    boolean updateRoom(StudyRoom room);

    /**
     * 删除自习室
     */
    boolean deleteRoom(Long roomId);

    /**
     * 获取自习室详情
     */
    StudyRoom getRoomById(Long roomId);

    /**
     * 获取所有自习室
     */
    List<StudyRoom> getAllRooms();

    /**
     * 分页查询自习室
     */
    IPage<StudyRoom> pageRooms(Page<StudyRoom> page, String building, Integer floor, String status);

    /**
     * 根据建筑查询自习室
     */
    List<StudyRoom> getRoomsByBuilding(String building);

    /**
     * 根据楼层查询自习室
     */
    List<StudyRoom> getRoomsByFloor(Integer floor);

    /**
     * 获取可用座位数
     */
    Integer getAvailableSeats(Long roomId);

    /**
     * 更新自习室状态
     */
    boolean updateRoomStatus(Long roomId, String status);

    /**
     * 统计各建筑自习室数量
     */
    List<StudyRoom> getRoomStatistics();
}