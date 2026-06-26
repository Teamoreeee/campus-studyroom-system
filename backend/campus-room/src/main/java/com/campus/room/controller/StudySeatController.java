package com.campus.room.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.room.dto.SeatDTO;
import com.campus.room.entity.StudySeat;
import com.campus.room.service.StudySeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/seat")
@RequiredArgsConstructor
@Tag(name = "座位管理", description = "座位信息管理")
public class StudySeatController {

    private final StudySeatService studySeatService;

    @PostMapping
    @Operation(summary = "创建座位")
    public ResponseEntity<String> createSeat(@RequestBody StudySeat seat) {
        boolean success = studySeatService.createSeat(seat);
        return success ? ResponseEntity.ok("创建成功") : ResponseEntity.badRequest().body("创建失败");
    }

    @PostMapping("/batch")
    @Operation(summary = "批量创建座位")
    public ResponseEntity<String> createSeatsBatch(@RequestBody List<StudySeat> seats) {
        boolean success = studySeatService.createSeatsBatch(seats);
        return success ? ResponseEntity.ok("批量创建成功") : ResponseEntity.badRequest().body("批量创建失败");
    }

    @PutMapping("/{seatId}")
    @Operation(summary = "更新座位信息")
    public ResponseEntity<String> updateSeat(@PathVariable Long seatId, @RequestBody StudySeat seat) {
        seat.setSeatId(seatId);
        boolean success = studySeatService.updateSeat(seat);
        return success ? ResponseEntity.ok("更新成功") : ResponseEntity.badRequest().body("更新失败");
    }

    @DeleteMapping("/{seatId}")
    @Operation(summary = "删除座位")
    public ResponseEntity<String> deleteSeat(@PathVariable Long seatId) {
        boolean success = studySeatService.deleteSeat(seatId);
        return success ? ResponseEntity.ok("删除成功") : ResponseEntity.badRequest().body("删除失败");
    }

    @GetMapping("/{seatId}")
    @Operation(summary = "获取座位详情")
    public ResponseEntity<SeatDTO> getSeat(@PathVariable Long seatId) {
        StudySeat seat = studySeatService.getSeatById(seatId);
        if (seat == null) {
            return ResponseEntity.notFound().build();
        }
        SeatDTO dto = new SeatDTO();
        BeanUtils.copyProperties(seat, dto);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/room/{roomId}")
    @Operation(summary = "根据房间ID获取座位列表")
    public ResponseEntity<List<SeatDTO>> getSeatsByRoomId(@PathVariable Long roomId) {
        List<StudySeat> seats = studySeatService.getSeatsByRoomId(roomId);
        List<SeatDTO> dtoList = seats.stream()
                .map(seat -> {
                    SeatDTO dto = new SeatDTO();
                    BeanUtils.copyProperties(seat, dto);
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/room/{roomId}/zone/{zone}")
    @Operation(summary = "根据区域获取座位")
    public ResponseEntity<List<SeatDTO>> getSeatsByZone(@PathVariable Long roomId, @PathVariable String zone) {
        List<StudySeat> seats = studySeatService.getSeatsByZone(roomId, zone);
        List<SeatDTO> dtoList = seats.stream()
                .map(seat -> {
                    SeatDTO dto = new SeatDTO();
                    BeanUtils.copyProperties(seat, dto);
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/room/{roomId}/available")
    @Operation(summary = "获取可用座位")
    public ResponseEntity<List<SeatDTO>> getAvailableSeats(@PathVariable Long roomId) {
        List<StudySeat> seats = studySeatService.getAvailableSeats(roomId);
        List<SeatDTO> dtoList = seats.stream()
                .map(seat -> {
                    SeatDTO dto = new SeatDTO();
                    BeanUtils.copyProperties(seat, dto);
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/room/{roomId}/type/{type}")
    @Operation(summary = "根据类型获取座位")
    public ResponseEntity<List<SeatDTO>> getSeatsByType(@PathVariable Long roomId, @PathVariable String type) {
        List<StudySeat> seats = studySeatService.getSeatsByType(roomId, type);
        List<SeatDTO> dtoList = seats.stream()
                .map(seat -> {
                    SeatDTO dto = new SeatDTO();
                    BeanUtils.copyProperties(seat, dto);
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询座位")
    public ResponseEntity<IPage<SeatDTO>> pageSeats(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) String zone,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type) {

        Page<StudySeat> page = new Page<>(current, size);
        IPage<StudySeat> seatPage = studySeatService.pageSeats(page, roomId, zone, status, type);

        IPage<SeatDTO> dtoPage = seatPage.convert(seat -> {
            SeatDTO dto = new SeatDTO();
            BeanUtils.copyProperties(seat, dto);
            return dto;
        });

        return ResponseEntity.ok(dtoPage);
    }

    @PutMapping("/{seatId}/status")
    @Operation(summary = "更新座位状态")
    public ResponseEntity<String> updateSeatStatus(@PathVariable Long seatId,
                                                  @RequestParam String status,
                                                  @RequestParam(required = false) Long userId,
                                                  @RequestParam(required = false) Long reservationId) {
        boolean success = studySeatService.updateSeatStatus(seatId, status, userId, reservationId);
        return success ? ResponseEntity.ok("状态更新成功") : ResponseEntity.badRequest().body("状态更新失败");
    }

    @PutMapping("/{seatId}/occupy")
    @Operation(summary = "占用座位")
    public ResponseEntity<String> occupySeat(@PathVariable Long seatId,
                                           @RequestParam Long userId,
                                           @RequestParam Long reservationId) {
        boolean success = studySeatService.occupySeat(seatId, userId, reservationId);
        return success ? ResponseEntity.ok("座位占用成功") : ResponseEntity.badRequest().body("座位占用失败");
    }

    @PutMapping("/{seatId}/release")
    @Operation(summary = "释放座位")
    public ResponseEntity<String> releaseSeat(@PathVariable Long seatId) {
        boolean success = studySeatService.releaseSeat(seatId);
        return success ? ResponseEntity.ok("座位释放成功") : ResponseEntity.badRequest().body("座位释放失败");
    }

    @GetMapping("/{roomId}/usage-statistics")
    @Operation(summary = "获取座位使用统计")
    public ResponseEntity<List<SeatDTO>> getSeatUsageStatistics(@PathVariable Long roomId) {
        List<StudySeat> seats = studySeatService.getSeatUsageStatistics(roomId);
        List<SeatDTO> dtoList = seats.stream()
                .map(seat -> {
                    SeatDTO dto = new SeatDTO();
                    BeanUtils.copyProperties(seat, dto);
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/{seatId}/available")
    @Operation(summary = "检查座位是否可用")
    public ResponseEntity<Boolean> isSeatAvailable(@PathVariable Long seatId) {
        boolean available = studySeatService.isSeatAvailable(seatId);
        return ResponseEntity.ok(available);
    }
}