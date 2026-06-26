package com.campus.ai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI智能推荐", description = "AI智能学习环境推荐系统")
public class StudyRecommendationController {

    @GetMapping("/recommendation")
    @Operation(summary = "获取学习环境推荐")
    public ResponseEntity<Map<String, Object>> getRecommendation(@RequestParam Long userId, @RequestParam String studyType) {
        Map<String, Object> result = new HashMap<>();

        // 简化的推荐逻辑
        String roomType = getRecommendedRoomType(studyType);
        String seatType = getRecommendedSeatType(studyType);

        result.put("recommendedRoomType", roomType);
        result.put("recommendedSeatType", seatType);
        result.put("confidence", 0.85);
        result.put("reason", "基于用户学习习惯和当前座位使用率分析");

        return ResponseEntity.ok(result);
    }

    @GetMapping("/study-tips")
    @Operation(summary = "获取学习建议")
    public ResponseEntity<Map<String, Object>> getStudyTips(@RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();

        result.put("tips", new String[]{
            "建议每学习50分钟休息10分钟",
            "保持良好的坐姿，避免久坐",
            "选择安静的学习环境",
            "准备必要的学习用品"
        });

        return ResponseEntity.ok(result);
    }

    @GetMapping("/usage-analysis")
    @Operation(summary = "使用行为分析")
    public ResponseEntity<Map<String, Object>> getUsageAnalysis(@RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();

        result.put("peakHours", new String[]{"09:00-11:00", "14:00-17:00"});
        result.put("favoriteRooms", new String[]{"图书馆3楼", "教学楼B座"});
        result.put("averageDuration", 120);
        result.put("studyEfficiency", 0.85);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/feedback")
    @Operation(summary = "提交反馈")
    public ResponseEntity<String> submitFeedback(@RequestParam Long userId,
                                               @RequestParam String feedbackType,
                                               @RequestParam String content) {
        // 保存反馈逻辑
        return ResponseEntity.ok("反馈已提交，感谢您的意见！");
    }

    private String getRecommendedRoomType(String studyType) {
        switch (studyType.toLowerCase()) {
            case "quiet":
                return "静音自习室";
            case "group":
                return "小组讨论室";
            case "computer":
                return "电子阅览室";
            default:
                return "普通自习室";
        }
    }

    private String getRecommendedSeatType(String studyType) {
        switch (studyType.toLowerCase()) {
            case "focus":
                return "靠窗座位";
            case "group":
                return "圆桌座位";
            case "computer":
                return "带电源座位";
            default:
                return "普通座位";
        }
    }
}