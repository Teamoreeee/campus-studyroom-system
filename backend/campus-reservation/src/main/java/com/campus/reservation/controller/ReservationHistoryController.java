package com.campus.reservation.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.reservation.dto.ReservationHistoryDTO;
import com.campus.reservation.entity.ReservationHistory;
import com.campus.reservation.service.ReservationHistoryService;
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
@RequestMapping("/api/reservation/history")
@RequiredArgsConstructor
@Tag(name = "预约历史", description = "预约历史记录管理")
public class ReservationHistoryController {

    private final ReservationHistoryService reservationHistoryService;

    @PostMapping
    @Operation(summary = "创建预约历史记录")
    public ResponseEntity<String> createHistory(@RequestBody ReservationHistory history) {
        boolean success = reservationHistoryService.createHistory(history);
        return success ? ResponseEntity.ok("历史记录创建成功") : ResponseEntity.badRequest().body("历史记录创建失败");
    }

    @PostMapping("/batch")
    @Operation(summary = "批量创建历史记录")
    public ResponseEntity<String> createHistoryBatch(@RequestBody List<ReservationHistory> histories) {
        boolean success = reservationHistoryService.createHistoryBatch(histories);
        return success ? ResponseEntity.ok("批量历史记录创建成功") : ResponseEntity.badRequest().body("批量历史记录创建失败");
    }

    @GetMapping("/reservation/{reservationId}")
    @Operation(summary = "根据预约ID获取历史记录")
    public ResponseEntity<List<ReservationHistoryDTO>> getHistoryByReservationId(@PathVariable Long reservationId) {
        List<ReservationHistory> histories = reservationHistoryService.getHistoryByReservationId(reservationId);
        List<ReservationHistoryDTO> dtoList = histories.stream()
                .map(history -> {
                    ReservationHistoryDTO dto = new ReservationHistoryDTO();
                    BeanUtils.copyProperties(history, dto);
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询历史记录")
    public ResponseEntity<IPage<ReservationHistoryDTO>> pageHistory(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long reservationId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime) {

        Page<ReservationHistory> page = new Page<>(current, size);
        IPage<ReservationHistory> historyPage = reservationHistoryService.pageHistory(page, reservationId, action, startTime, endTime);

        IPage<ReservationHistoryDTO> dtoPage = historyPage.convert(history -> {
            ReservationHistoryDTO dto = new ReservationHistoryDTO();
            BeanUtils.copyProperties(history, dto);
            return dto;
        });

        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户预约历史")
    public ResponseEntity<List<ReservationHistoryDTO>> getUserReservationHistory(@PathVariable Long userId, @RequestParam(defaultValue = "30") Integer days) {
        List<ReservationHistory> histories = reservationHistoryService.getUserReservationHistory(userId, days);
        List<ReservationHistoryDTO> dtoList = histories.stream()
                .map(history -> {
                    ReservationHistoryDTO dto = new ReservationHistoryDTO();
                    BeanUtils.copyProperties(history, dto);
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/room/{roomId}")
    @Operation(summary = "获取房间预约历史")
    public ResponseEntity<List<ReservationHistoryDTO>> getRoomReservationHistory(@PathVariable Long roomId, @RequestParam(defaultValue = "30") Integer days) {
        List<ReservationHistory> histories = reservationHistoryService.getRoomReservationHistory(roomId, days);
        List<ReservationHistoryDTO> dtoList = histories.stream()
                .map(history -> {
                    ReservationHistoryDTO dto = new ReservationHistoryDTO();
                    BeanUtils.copyProperties(history, dto);
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @PostMapping("/status-change")
    @Operation(summary = "记录状态变更")
    public ResponseEntity<String> recordStatusChange(@RequestParam Long reservationId,
                                                    @RequestParam String action,
                                                    @RequestParam Long actionUser,
                                                    @RequestParam(required = false) String notes) {
        boolean success = reservationHistoryService.recordStatusChange(reservationId, action, actionUser, notes);
        return success ? ResponseEntity.ok("状态变更记录成功") : ResponseEntity.badRequest().body("状态变更记录失败");
    }
}