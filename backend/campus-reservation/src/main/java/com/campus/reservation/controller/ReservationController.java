package com.campus.reservation.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.reservation.dto.ReservationDTO;
import com.campus.reservation.entity.Reservation;
import com.campus.reservation.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservation")
@RequiredArgsConstructor
@Tag(name = "预约管理", description = "自习室座位预约管理")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @Operation(summary = "创建预约")
    public ResponseEntity<String> createReservation(@RequestBody Reservation reservation) {
        boolean success = reservationService.createReservation(reservation);
        return success ? ResponseEntity.ok("预约创建成功") : ResponseEntity.badRequest().body("预约创建失败");
    }

    @PostMapping("/batch")
    @Operation(summary = "批量创建预约")
    public ResponseEntity<String> createReservationBatch(@RequestBody List<Reservation> reservations) {
        boolean success = reservationService.createReservationBatch(reservations);
        return success ? ResponseEntity.ok("批量预约创建成功") : ResponseEntity.badRequest().body("批量预约创建失败");
    }

    @PutMapping("/{reservationId}")
    @Operation(summary = "更新预约信息")
    public ResponseEntity<String> updateReservation(@PathVariable Long reservationId, @RequestBody Reservation reservation) {
        reservation.setReservationId(reservationId);
        boolean success = reservationService.updateReservation(reservation);
        return success ? ResponseEntity.ok("预约更新成功") : ResponseEntity.badRequest().body("预约更新失败");
    }

    @DeleteMapping("/{reservationId}")
    @Operation(summary = "删除预约")
    public ResponseEntity<String> deleteReservation(@PathVariable Long reservationId) {
        boolean success = reservationService.deleteReservation(reservationId);
        return success ? ResponseEntity.ok("预约删除成功") : ResponseEntity.badRequest().body("预约删除失败");
    }

    @GetMapping("/{reservationId}")
    @Operation(summary = "获取预约详情")
    public ResponseEntity<ReservationDTO> getReservation(@PathVariable Long reservationId) {
        Reservation reservation = reservationService.getReservationById(reservationId);
        if (reservation == null) {
            return ResponseEntity.notFound().build();
        }
        ReservationDTO dto = new ReservationDTO();
        BeanUtils.copyProperties(reservation, dto);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "根据用户ID获取预约列表")
    public ResponseEntity<List<ReservationDTO>> getReservationsByUserId(@PathVariable Long userId) {
        List<Reservation> reservations = reservationService.getReservationsByUserId(userId);
        List<ReservationDTO> dtoList = reservations.stream()
                .map(reservation -> {
                    ReservationDTO dto = new ReservationDTO();
                    BeanUtils.copyProperties(reservation, dto);
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/room/{roomId}")
    @Operation(summary = "根据房间ID获取预约列表")
    public ResponseEntity<List<ReservationDTO>> getReservationsByRoomId(@PathVariable Long roomId) {
        List<Reservation> reservations = reservationService.getReservationsByRoomId(roomId);
        List<ReservationDTO> dtoList = reservations.stream()
                .map(reservation -> {
                    ReservationDTO dto = new ReservationDTO();
                    BeanUtils.copyProperties(reservation, dto);
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/seat/{seatId}")
    @Operation(summary = "根据座位ID获取预约列表")
    public ResponseEntity<List<ReservationDTO>> getReservationsBySeatId(@PathVariable Long seatId) {
        List<Reservation> reservations = reservationService.getReservationsBySeatId(seatId);
        List<ReservationDTO> dtoList = reservations.stream()
                .map(reservation -> {
                    ReservationDTO dto = new ReservationDTO();
                    BeanUtils.copyProperties(reservation, dto);
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询预约")
    public ResponseEntity<IPage<ReservationDTO>> pageReservations(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime) {

        Page<Reservation> page = new Page<>(current, size);
        IPage<Reservation> reservationPage = reservationService.pageReservations(page, userId, roomId, status, startTime, endTime);

        IPage<ReservationDTO> dtoPage = reservationPage.convert(reservation -> {
            ReservationDTO dto = new ReservationDTO();
            BeanUtils.copyProperties(reservation, dto);
            return dto;
        });

        return ResponseEntity.ok(dtoPage);
    }

    @PutMapping("/{reservationId}/confirm")
    @Operation(summary = "确认预约")
    public ResponseEntity<String> confirmReservation(@PathVariable Long reservationId) {
        boolean success = reservationService.confirmReservation(reservationId);
        return success ? ResponseEntity.ok("预约确认成功") : ResponseEntity.badRequest().body("预约确认失败");
    }

    @PutMapping("/{reservationId}/cancel")
    @Operation(summary = "取消预约")
    public ResponseEntity<String> cancelReservation(@PathVariable Long reservationId, @RequestParam String reason) {
        boolean success = reservationService.cancelReservation(reservationId, reason);
        return success ? ResponseEntity.ok("预约取消成功") : ResponseEntity.badRequest().body("预约取消失败");
    }

    @PutMapping("/{reservationId}/complete")
    @Operation(summary = "完成预约")
    public ResponseEntity<String> completeReservation(@PathVariable Long reservationId) {
        boolean success = reservationService.completeReservation(reservationId);
        return success ? ResponseEntity.ok("预约完成成功") : ResponseEntity.badRequest().body("预约完成失败");
    }

    @PutMapping("/{reservationId}/timeout-cancel")
    @Operation(summary = "超时取消预约")
    public ResponseEntity<String> timeoutCancelReservation(@PathVariable Long reservationId) {
        boolean success = reservationService.timeoutCancelReservation(reservationId);
        return success ? ResponseEntity.ok("超时取消成功") : ResponseEntity.badRequest().body("超时取消失败");
    }

    @PostMapping("/{reservationId}/qrcode")
    @Operation(summary = "生成预约二维码")
    public ResponseEntity<String> generateQRCode(@PathVariable Long reservationId) {
        String qrcode = reservationService.generateQRCode(reservationId);
        return ResponseEntity.ok(qrcode);
    }

    @GetMapping("/qrcode/{qrcode}")
    @Operation(summary = "根据二维码验证预约")
    public ResponseEntity<ReservationDTO> verifyQRCode(@PathVariable String qrcode) {
        Reservation reservation = reservationService.verifyQRCode(qrcode);
        if (reservation == null) {
            return ResponseEntity.notFound().build();
        }
        ReservationDTO dto = new ReservationDTO();
        BeanUtils.copyProperties(reservation, dto);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/user/{userId}/active")
    @Operation(summary = "获取用户当前有效的预约")
    public ResponseEntity<ReservationDTO> getUserActiveReservation(@PathVariable Long userId) {
        Reservation reservation = reservationService.getUserActiveReservation(userId);
        if (reservation == null) {
            return ResponseEntity.notFound().build();
        }
        ReservationDTO dto = new ReservationDTO();
        BeanUtils.copyProperties(reservation, dto);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/room/{roomId}/daily-stats")
    @Operation(summary = "获取房间今日预约统计")
    public ResponseEntity<List<ReservationDTO>> getRoomDailyStatistics(@PathVariable Long roomId, @RequestParam String date) {
        // 这里需要将字符串转换为LocalDate，简化处理
        List<Reservation> reservations = reservationService.getRoomDailyStatistics(roomId, java.time.LocalDate.parse(date));
        List<ReservationDTO> dtoList = reservations.stream()
                .map(reservation -> {
                    ReservationDTO dto = new ReservationDTO();
                    BeanUtils.copyProperties(reservation, dto);
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/seat/{seatId}/usage-stats")
    @Operation(summary = "获取座位使用率统计")
    public ResponseEntity<List<ReservationDTO>> getSeatUsageStatistics(@PathVariable Long seatId, @RequestParam(defaultValue = "7") Integer days) {
        List<Reservation> reservations = reservationService.getSeatUsageStatistics(seatId, days);
        List<ReservationDTO> dtoList = reservations.stream()
                .map(reservation -> {
                    ReservationDTO dto = new ReservationDTO();
                    BeanUtils.copyProperties(reservation, dto);
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
}