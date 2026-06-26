package com.campus.attendance.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.attendance.common.Result;
import com.campus.attendance.entity.Attendance;
import com.campus.attendance.service.AttendanceService;
import com.campus.attendance.utils.JwtUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@Tag(name = "考勤管理", description = "签到签退与考勤记录")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final JwtUtils jwtUtils;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/check-in")
    @Operation(summary = "签到")
    public Result<Map<String, Object>> checkIn(@RequestBody CheckInRequest request, HttpServletRequest httpRequest) {
        Long userId = getUserId(httpRequest);

        if (request.getReservationId() == null) {
            return Result.error("请选择要签到的预约");
        }

        // 查询预约信息并校验
        Map<String, Long> info = fetchReservationInfo(request.getReservationId());
        Long roomId = info.get("roomId");
        Long seatId = info.get("seatId");
        Long resUserId = info.get("userId");

        if (roomId == null || seatId == null || roomId == 0L || seatId == 0L) {
            return Result.error("预约不存在，无法签到");
        }

        if (!Objects.equals(resUserId, userId)) {
            return Result.error("只能签到自己的预约");
        }

        boolean success = attendanceService.checkIn(userId, request.getReservationId(), roomId, seatId, "manual", null, null);

        Map<String, Object> data = new HashMap<>();
        data.put("success", success);
        return Result.success(data);
    }

    private Map<String, Long> fetchReservationInfo(Long reservationId) {
        Map<String, Long> result = new HashMap<>();
        try {
            String url = "http://localhost:8004/api/reservation/" + reservationId;
            String response = restTemplate.getForObject(url, String.class);
            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                JsonNode data = root.path("data");
                if (data.isObject()) {
                    result.put("roomId", data.path("roomId").asLong());
                    result.put("seatId", data.path("seatId").asLong());
                    result.put("userId", data.path("userId").asLong());
                }
            }
        } catch (Exception e) {
            // 查询失败则返回空，由上层判断
        }
        return result;
    }

    @PostMapping("/check-out")
    @Operation(summary = "签退")
    public Result<Map<String, Object>> checkOut(@RequestBody CheckOutRequest request, HttpServletRequest httpRequest) {
        Long userId = getUserId(httpRequest);

        if (request.getReservationId() == null) {
            return Result.error("请选择要签退的预约");
        }

        boolean success = attendanceService.checkOutByReservation(userId, request.getReservationId(), "manual", null, null);

        Map<String, Object> data = new HashMap<>();
        data.put("success", success);
        return Result.success(data);
    }

    @GetMapping("/my")
    @Operation(summary = "我的考勤记录")
    public Result<PageResult<AttendanceVO>> getMyAttendance(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            HttpServletRequest httpRequest) {
        Long userId = getUserId(httpRequest);
        Page<Attendance> p = new Page<>(page, size);
        IPage<Attendance> result = attendanceService.pageAttendance(p, userId, null, null, status, null, null);

        List<AttendanceVO> list = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(new PageResult<>(list, result.getTotal(), page, size));
    }

    @GetMapping("/today-stats")
    @Operation(summary = "今日考勤统计")
    public Result<Map<String, Integer>> getTodayStats(HttpServletRequest httpRequest) {
        Long userId = getUserId(httpRequest);
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(23, 59, 59);
        List<Attendance> list = attendanceService.getAttendanceByUserId(userId).stream()
                .filter(a -> a.getCheckInTime() != null
                        && !a.getCheckInTime().isBefore(start)
                        && !a.getCheckInTime().isAfter(end))
                .collect(Collectors.toList());

        int checkedIn = (int) list.stream().filter(a -> a.getCheckOutTime() != null).count();
        int notCheckedIn = list.size() - checkedIn;
        int lateCount = 0;

        Map<String, Integer> stats = new HashMap<>();
        stats.put("totalReservations", list.size());
        stats.put("checkedIn", checkedIn);
        stats.put("notCheckedIn", notCheckedIn);
        stats.put("lateCount", lateCount);
        return Result.success(stats);
    }

    private AttendanceVO convertToVO(Attendance attendance) {
        AttendanceVO vo = new AttendanceVO();
        vo.setAttendanceId(attendance.getAttendanceId());
        vo.setReservationId(attendance.getReservationId());
        vo.setUserId(attendance.getUserId());
        vo.setCheckInTime(attendance.getCheckInTime() != null ? attendance.getCheckInTime().toString() : null);
        vo.setCheckOutTime(attendance.getCheckOutTime() != null ? attendance.getCheckOutTime().toString() : null);
        vo.setDuration(attendance.getDurationMinutes() != null ? attendance.getDurationMinutes() : 0);
        vo.setStatus(convertStatus(attendance.getStatus()));
        return vo;
    }

    private String convertStatus(String status) {
        return switch (status) {
            case "active" -> "NORMAL";
            case "completed" -> "NORMAL";
            case "timeout" -> "ABSENT";
            default -> "NORMAL";
        };
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtils.getUserIdFromToken(token);
        }
        return null;
    }

    @lombok.Data
    public static class CheckInRequest {
        private Long reservationId;
        private Long roomId;
        private Long seatId;
        private String qrCode;
    }

    @lombok.Data
    public static class CheckOutRequest {
        private Long reservationId;
    }

    @lombok.Data
    public static class AttendanceVO {
        private Long attendanceId;
        private Long reservationId;
        private Long userId;
        private String checkInTime;
        private String checkOutTime;
        private Integer duration;
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
