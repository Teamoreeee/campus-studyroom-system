package com.campus.room.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.room.entity.StudyRoom;
import com.campus.room.mapper.StudyRoomMapper;
import com.campus.room.service.StudyRoomService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class StudyRoomServiceImpl extends ServiceImpl<StudyRoomMapper, StudyRoom> implements StudyRoomService {

    @Override
    public boolean createRoom(StudyRoom room) {
        room.setStatus("open"); // 默认开放状态
        return save(room);
    }

    @Override
    public boolean updateRoom(StudyRoom room) {
        return updateById(room);
    }

    @Override
    public boolean deleteRoom(Long roomId) {
        return removeById(roomId);
    }

    @Override
    public StudyRoom getRoomById(Long roomId) {
        return getById(roomId);
    }

    @Override
    public List<StudyRoom> getAllRooms() {
        return list();
    }

    @Override
    public IPage<StudyRoom> pageRooms(Page<StudyRoom> page, String building, Integer floor, String status) {
        LambdaQueryWrapper<StudyRoom> wrapper = new LambdaQueryWrapper<>();

        if (Objects.nonNull(building)) {
            wrapper.eq(StudyRoom::getBuilding, building);
        }

        if (Objects.nonNull(floor)) {
            wrapper.eq(StudyRoom::getFloor, floor);
        }

        if (Objects.nonNull(status)) {
            wrapper.eq(StudyRoom::getStatus, status);
        }

        wrapper.eq(StudyRoom::getDeleted, 0);
        wrapper.orderByDesc(StudyRoom::getCreateTime);

        return page(page, wrapper);
    }

    @Override
    public List<StudyRoom> getRoomsByBuilding(String building) {
        LambdaQueryWrapper<StudyRoom> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyRoom::getBuilding, building)
                .eq(StudyRoom::getDeleted, 0)
                .orderByDesc(StudyRoom::getCreateTime);

        return list(wrapper);
    }

    @Override
    public List<StudyRoom> getRoomsByFloor(Integer floor) {
        LambdaQueryWrapper<StudyRoom> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyRoom::getFloor, floor)
                .eq(StudyRoom::getDeleted, 0)
                .orderByDesc(StudyRoom::getCreateTime);

        return list(wrapper);
    }

    @Override
    public Integer getAvailableSeats(Long roomId) {
        // 这里可以关联查询座位表获取可用座位数
        // 暂时返回房间容量
        StudyRoom room = getById(roomId);
        return room != null ? room.getCapacity() : 0;
    }

    @Override
    public boolean updateRoomStatus(Long roomId, String status) {
        StudyRoom room = getById(roomId);
        if (room != null) {
            room.setStatus(status);
            return updateById(room);
        }
        return false;
    }

    @Override
    public List<StudyRoom> getRoomStatistics() {
        return list(new LambdaQueryWrapper<StudyRoom>()
                .select(StudyRoom::getBuilding, StudyRoom::getRoomId)
                .groupBy(StudyRoom::getBuilding)
                .orderByAsc(StudyRoom::getBuilding));
    }
}