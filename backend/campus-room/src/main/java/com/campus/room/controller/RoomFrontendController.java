package com.campus.room.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.room.common.Result;
import com.campus.room.entity.StudyRoom;
import com.campus.room.entity.StudySeat;
import com.campus.room.service.StudyRoomService;
import com.campus.room.service.StudySeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/room")
@RequiredArgsConstructor
@Tag(name = "自习室前端接口", description = "面向前端页面的自习室查询接口")
public class RoomFrontendController {

    private final StudyRoomService studyRoomService;
    private final StudySeatService studySeatService;
    private final com.campus.room.mapper.ReservationStatusMapper reservationStatusMapper;

    @GetMapping("/rooms")
    @Operation(summary = "前端：分页查询自习室")
    public Result<PageResult<RoomVO>> listRooms(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String building) {

        Page<StudyRoom> p = new Page<>(page, size);
        IPage<StudyRoom> roomPage = studyRoomService.pageRooms(p, building, null, null);

        // 关键词过滤（内存过滤，简化实现）
        List<StudyRoom> records = roomPage.getRecords();
        if (keyword != null && !keyword.isEmpty()) {
            records = records.stream()
                    .filter(r -> r.getRoomName() != null && r.getRoomName().contains(keyword)
                            || r.getBuilding() != null && r.getBuilding().contains(keyword))
                    .collect(Collectors.toList());
        }

        List<RoomVO> list = records.stream().map(this::convertRoom).collect(Collectors.toList());
        return Result.success(new PageResult<>(list, roomPage.getTotal(), page, size));
    }

    @GetMapping("/rooms/{roomId}")
    @Operation(summary = "前端：获取自习室详情")
    public Result<RoomVO> getRoom(@PathVariable Long roomId) {
        StudyRoom room = studyRoomService.getRoomById(roomId);
        return Result.success(convertRoom(room));
    }

    @GetMapping("/rooms/{roomId}/seats")
    @Operation(summary = "前端：获取自习室座位")
    public Result<List<SeatVO>> getRoomSeats(@PathVariable Long roomId,
                                                 @RequestParam(required = false) String date,
                                                 @RequestParam(required = false) String startTime,
                                                 @RequestParam(required = false) String endTime) {
        List<StudySeat> seats = studySeatService.getSeatsByRoomId(roomId);
        // 查询指定日期指定时间段已被有效预约占用的座位
        List<Long> occupiedSeatIds = new ArrayList<>();
        if (date != null && !date.isEmpty() && startTime != null && !startTime.isEmpty()
                && endTime != null && !endTime.isEmpty()) {
            try {
                occupiedSeatIds = reservationStatusMapper.findOccupiedSeatIds(roomId, date, startTime, endTime);
            } catch (Exception e) {
                // 查询失败时降级处理，不阻断座位列表展示
            }
        }
        final List<Long> occupied = occupiedSeatIds;
        List<SeatVO> list = seats.stream().map(seat -> {
            SeatVO vo = convertSeat(seat);
            if (occupied.contains(seat.getSeatId())) {
                vo.setStatus("RESERVED");
            }
            return vo;
        }).collect(Collectors.toList());
        return Result.success(list);
    }

    @GetMapping("/buildings")
    @Operation(summary = "前端：获取所有教学楼")
    public Result<List<String>> getBuildings() {
        List<StudyRoom> rooms = studyRoomService.getAllRooms();
        List<String> buildings = rooms.stream()
                .map(StudyRoom::getBuilding)
                .distinct()
                .collect(Collectors.toList());
        return Result.success(buildings);
    }

    @GetMapping("/admin/rooms")
    @Operation(summary = "前端：管理员查询自习室")
    public Result<PageResult<RoomVO>> adminListRooms(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        return listRooms(page, size, keyword, null);
    }

    @PostMapping("/admin/rooms")
    @Operation(summary = "前端：管理员创建自习室")
    public Result<Boolean> adminCreateRoom(@RequestBody RoomVO vo) {
        StudyRoom room = new StudyRoom();
        BeanUtils.copyProperties(vo, room);
        return Result.success(studyRoomService.createRoom(room));
    }

    @PutMapping("/admin/rooms/{roomId}")
    @Operation(summary = "前端：管理员更新自习室")
    public Result<Boolean> adminUpdateRoom(@PathVariable Long roomId, @RequestBody RoomVO vo) {
        StudyRoom room = new StudyRoom();
        BeanUtils.copyProperties(vo, room);
        room.setRoomId(roomId);
        return Result.success(studyRoomService.updateRoom(room));
    }

    @DeleteMapping("/admin/rooms/{roomId}")
    @Operation(summary = "前端：管理员删除自习室")
    public Result<Boolean> adminDeleteRoom(@PathVariable Long roomId) {
        return Result.success(studyRoomService.deleteRoom(roomId));
    }

    private RoomVO convertRoom(StudyRoom room) {
        RoomVO vo = new RoomVO();
        BeanUtils.copyProperties(room, vo);
        // 兼容字段
        if (room.getStatus() instanceof String) {
            vo.setStatus("open".equals(room.getStatus()) ? 1 : 0);
        }
        // 座位数与可用座位数
        List<StudySeat> seats = studySeatService.getSeatsByRoomId(room.getRoomId());
        vo.setSeatCount(seats.size());
        int occupied = 0;
        try {
            occupied = reservationStatusMapper.countOccupiedSeatsToday(room.getRoomId());
        } catch (Exception e) {
            // 查询失败时降级为 0，不影响主流程
        }
        int available = seats.size() - occupied;
        vo.setAvailableSeats(Math.max(available, 0));
        return vo;
    }

    private SeatVO convertSeat(StudySeat seat) {
        SeatVO vo = new SeatVO();
        vo.setSeatId(seat.getSeatId());
        vo.setRoomId(seat.getRoomId());
        vo.setSeatNo(seat.getSeatNumber());
        vo.setSeatType(seat.getType() != null ? seat.getType().toUpperCase() : "NORMAL");
        vo.setPosition(seat.getZone());
        vo.setHasPower(seat.getHasPower() != null && seat.getHasPower());
        String status = seat.getStatus();
        vo.setStatus(switch (status) {
            case "available" -> "AVAILABLE";
            case "occupied" -> "IN_USE";
            case "reserved" -> "RESERVED";
            default -> "MAINTAINING";
        });
        return vo;
    }

    @lombok.Data
    public static class RoomVO {
        private Long roomId;
        private String roomName;
        private String building;
        private Integer floor;
        private Integer capacity;
        private Integer seatCount;
        private Integer availableSeats;
        private String facilities;
        private String openTime;
        private String closeTime;
        private Integer status;
    }

    @lombok.Data
    public static class SeatVO {
        private Long seatId;
        private Long roomId;
        private String seatNo;
        private String seatType;
        private String position;
        private Boolean hasPower;
        private String status;
    }

    @lombok.Data
    public static class PageResult<T> {
        private List<T> list;
        private Long total;
        private Integer page;
        private Integer size;

        public PageResult(List<T> list, Long total, Integer page, Integer size) {
            this.list = list;
            this.total = total;
            this.page = page;
            this.size = size;
        }
    }
}
