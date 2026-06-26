package com.campus.room.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.room.dto.RoomDTO;
import com.campus.room.entity.StudyRoom;
import com.campus.room.service.StudyRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/room")
@RequiredArgsConstructor
@Tag(name = "自习室管理", description = "自习室信息管理")
public class StudyRoomController {

    private final StudyRoomService studyRoomService;

    @PostMapping
    @Operation(summary = "创建自习室")
    public ResponseEntity<String> createRoom(@RequestBody StudyRoom room) {
        boolean success = studyRoomService.createRoom(room);
        return success ? ResponseEntity.ok("创建成功") : ResponseEntity.badRequest().body("创建失败");
    }

    @PutMapping("/{roomId}")
    @Operation(summary = "更新自习室信息")
    public ResponseEntity<String> updateRoom(@PathVariable Long roomId, @RequestBody StudyRoom room) {
        room.setRoomId(roomId);
        boolean success = studyRoomService.updateRoom(room);
        return success ? ResponseEntity.ok("更新成功") : ResponseEntity.badRequest().body("更新失败");
    }

    @DeleteMapping("/{roomId}")
    @Operation(summary = "删除自习室")
    public ResponseEntity<String> deleteRoom(@PathVariable Long roomId) {
        boolean success = studyRoomService.deleteRoom(roomId);
        return success ? ResponseEntity.ok("删除成功") : ResponseEntity.badRequest().body("删除失败");
    }

    @GetMapping("/{roomId}")
    @Operation(summary = "获取自习室详情")
    public ResponseEntity<RoomDTO> getRoom(@PathVariable Long roomId) {
        StudyRoom room = studyRoomService.getRoomById(roomId);
        if (room == null) {
            return ResponseEntity.notFound().build();
        }
        RoomDTO dto = new RoomDTO();
        BeanUtils.copyProperties(room, dto);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    @Operation(summary = "获取所有自习室")
    public ResponseEntity<List<RoomDTO>> getAllRooms() {
        List<StudyRoom> rooms = studyRoomService.getAllRooms();
        List<RoomDTO> dtoList = rooms.stream()
                .map(room -> {
                    RoomDTO dto = new RoomDTO();
                    BeanUtils.copyProperties(room, dto);
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询自习室")
    public ResponseEntity<IPage<RoomDTO>> pageRooms(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String building,
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) String status) {

        Page<StudyRoom> page = new Page<>(current, size);
        IPage<StudyRoom> roomPage = studyRoomService.pageRooms(page, building, floor, status);

        IPage<RoomDTO> dtoPage = roomPage.convert(room -> {
            RoomDTO dto = new RoomDTO();
            BeanUtils.copyProperties(room, dto);
            return dto;
        });

        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/building/{building}")
    @Operation(summary = "根据建筑查询自习室")
    public ResponseEntity<List<RoomDTO>> getRoomsByBuilding(@PathVariable String building) {
        List<StudyRoom> rooms = studyRoomService.getRoomsByBuilding(building);
        List<RoomDTO> dtoList = rooms.stream()
                .map(room -> {
                    RoomDTO dto = new RoomDTO();
                    BeanUtils.copyProperties(room, dto);
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/floor/{floor}")
    @Operation(summary = "根据楼层查询自习室")
    public ResponseEntity<List<RoomDTO>> getRoomsByFloor(@PathVariable Integer floor) {
        List<StudyRoom> rooms = studyRoomService.getRoomsByFloor(floor);
        List<RoomDTO> dtoList = rooms.stream()
                .map(room -> {
                    RoomDTO dto = new RoomDTO();
                    BeanUtils.copyProperties(room, dto);
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/{roomId}/available-seats")
    @Operation(summary = "获取可用座位数")
    public ResponseEntity<Integer> getAvailableSeats(@PathVariable Long roomId) {
        Integer availableSeats = studyRoomService.getAvailableSeats(roomId);
        return ResponseEntity.ok(availableSeats);
    }

    @PutMapping("/{roomId}/status")
    @Operation(summary = "更新自习室状态")
    public ResponseEntity<String> updateRoomStatus(@PathVariable Long roomId, @RequestParam String status) {
        boolean success = studyRoomService.updateRoomStatus(roomId, status);
        return success ? ResponseEntity.ok("状态更新成功") : ResponseEntity.badRequest().body("状态更新失败");
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取自习室统计信息")
    public ResponseEntity<List<RoomDTO>> getRoomStatistics() {
        List<StudyRoom> rooms = studyRoomService.getRoomStatistics();
        List<RoomDTO> dtoList = rooms.stream()
                .map(room -> {
                    RoomDTO dto = new RoomDTO();
                    BeanUtils.copyProperties(room, dto);
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
}