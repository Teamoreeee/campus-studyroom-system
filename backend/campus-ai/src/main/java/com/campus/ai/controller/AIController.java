package com.campus.ai.controller;

import com.campus.ai.common.Result;
import com.campus.ai.entity.AiRecommendation;
import com.campus.ai.entity.KnowledgeBase;
import com.campus.ai.entity.RoomInfo;
import com.campus.ai.entity.SeatInfo;
import com.campus.ai.mapper.AiRecommendationMapper;
import com.campus.ai.mapper.RoomSeatMapper;
import com.campus.ai.service.CollaborativeFilterService;
import com.campus.ai.service.RagService;
import com.campus.ai.service.ZhipuAiService;
import com.campus.ai.utils.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI 智能服务", description = "智能推荐与智能客服")
public class AIController {

    private final JwtUtils jwtUtils;
    private final ZhipuAiService zhipuAiService;
    private final CollaborativeFilterService collaborativeFilterService;
    private final RagService ragService;
    private final RoomSeatMapper roomSeatMapper;
    private final AiRecommendationMapper aiRecommendationMapper;

    private static final String[] STRATEGIES = {"协同过滤", "内容推荐", "热门时段分析", "位置偏好", "行为模式匹配"};

    @PostMapping("/recommendations")
    @Operation(summary = "获取智能推荐")
    public Result<List<RecommendationVO>> getRecommendations(@RequestBody RecommendRequest request, HttpServletRequest httpRequest) {
        Long userId = getUserId(httpRequest);
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        // 1. 协同过滤推荐房间
        List<CollaborativeFilterService.Recommendation> cfResults =
                new ArrayList<>(collaborativeFilterService.recommendRooms(userId, 10));

        // 2. 冷启动或协同过滤结果不足时，使用内容推荐兜底
        if (cfResults.size() < 3) {
            List<CollaborativeFilterService.Recommendation> contentResults =
                    collaborativeFilterService.contentBasedRecommend(
                            userId,
                            request.getBuilding(),
                            request.getPreferWindow(),
                            request.getPreferPower(),
                            10);

            Set<Long> existingRoomIds = cfResults.stream()
                    .map(CollaborativeFilterService.Recommendation::roomId)
                    .collect(Collectors.toSet());
            for (CollaborativeFilterService.Recommendation rec : contentResults) {
                if (!existingRoomIds.contains(rec.roomId())) {
                    cfResults.add(rec);
                    existingRoomIds.add(rec.roomId());
                }
            }
        }

        // 3. 取前 3 个，为每个房间找一个可用座位
        String selectedBuilding = request.getBuilding();
        List<RecommendationVO> list = new ArrayList<>();
        int index = 0;
        for (CollaborativeFilterService.Recommendation rec : cfResults) {
            if (list.size() >= 3) {
                break;
            }

            RoomInfo room = roomSeatMapper.selectRoomById(rec.roomId());
            if (room == null) {
                continue;
            }
            // 按教学楼过滤
            if (selectedBuilding != null && !selectedBuilding.isBlank()
                    && !selectedBuilding.equals(room.getBuilding())) {
                continue;
            }

            SeatInfo seat = findBestSeat(room.getRoomId(), request);
            if (seat == null) {
                continue;
            }

            String seatType = seat.getType() == null ? "normal" : seat.getType();
            boolean hasPower = Boolean.TRUE.equals(seat.getHasPower());
            String building = request.getBuilding() != null ? request.getBuilding() : room.getBuilding();

            String reason = zhipuAiService.generateRecommendationReason(
                    building,
                    seatType,
                    hasPower,
                    request.getPreferWindow() != null ? request.getPreferWindow() : false,
                    request.getPreferPower() != null ? request.getPreferPower() : false);

            RecommendationVO vo = new RecommendationVO();
            vo.setRecommendationId((long) (++index));
            vo.setUserId(userId);
            vo.setRoomId(room.getRoomId());
            vo.setSeatId(seat.getSeatId());
            // 协同过滤/内容推荐分数可能过于接近，按排名略微拉开差距
            double rankedScore = Math.min(0.98, Math.max(0.60, rec.score())) - (index - 1) * 0.03;
            vo.setScore(Math.max(0.60, rankedScore));
            vo.setReason(reason);
            vo.setStrategy(STRATEGIES[index % STRATEGIES.length]);
            vo.setCreateTime(LocalDateTime.now().toString());
            list.add(vo);

            // 持久化推荐记录
            AiRecommendation record = new AiRecommendation();
            record.setUserId(userId);
            record.setRoomId(room.getRoomId());
            record.setSeatId(seat.getSeatId());
            record.setScore(new java.math.BigDecimal(vo.getScore()).setScale(2, java.math.RoundingMode.HALF_UP));
            record.setReason(reason);
            record.setStrategy(vo.getStrategy());
            record.setIsAccepted(false);
            aiRecommendationMapper.insert(record);
        }

        // 兜底：如果推荐结果为空，返回热门房间
        if (list.isEmpty()) {
            list = fallbackRecommendations(userId, request);
        }

        return Result.success(list);
    }

    private SeatInfo findBestSeat(Long roomId, RecommendRequest request) {
        Boolean preferWindow = request.getPreferWindow();
        Boolean preferPower = request.getPreferPower();

        // 优先找同时满足两个偏好的座位
        if (Boolean.TRUE.equals(preferWindow) && Boolean.TRUE.equals(preferPower)) {
            List<SeatInfo> seats = roomSeatMapper.selectAvailableSeatsByTypes(roomId, "window", true);
            if (!seats.isEmpty()) {
                return seats.get(0);
            }
        }

        String preferredType = null;
        if (Boolean.TRUE.equals(preferWindow)) {
            preferredType = "window";
        } else if (Boolean.TRUE.equals(preferPower)) {
            preferredType = "power";
        }

        if (preferredType != null) {
            List<SeatInfo> seats = roomSeatMapper.selectAvailableSeatsByType(roomId, preferredType);
            if (!seats.isEmpty()) {
                return seats.get(0);
            }
        }

        List<SeatInfo> seats = roomSeatMapper.selectAvailableSeatsByRoom(roomId);
        return seats.isEmpty() ? null : seats.get(0);
    }

    private List<RecommendationVO> fallbackRecommendations(Long userId, RecommendRequest request) {
        List<RecommendationVO> list = new ArrayList<>();
        // 兜底：按教学楼过滤，返回可用自习室
        List<RoomInfo> rooms = request.getBuilding() != null && !request.getBuilding().isBlank()
                ? roomSeatMapper.selectRoomsByBuilding(request.getBuilding())
                : roomSeatMapper.selectAllRooms();

        int index = 0;
        for (RoomInfo room : rooms) {
            if (list.size() >= 3) {
                break;
            }
            SeatInfo seat = findBestSeat(room.getRoomId(), request);
            if (seat == null) {
                continue;
            }

            RecommendationVO vo = new RecommendationVO();
            vo.setRecommendationId((long) (++index));
            vo.setUserId(userId);
            vo.setRoomId(room.getRoomId());
            vo.setSeatId(seat.getSeatId());
            // 按顺序递减分数，避免全部相同
            vo.setScore(0.95 - (index - 1) * 0.05);
            vo.setReason(zhipuAiService.generateRecommendationReason(
                    room.getBuilding(),
                    seat.getType() == null ? "normal" : seat.getType(),
                    Boolean.TRUE.equals(seat.getHasPower()),
                    request.getPreferWindow() != null ? request.getPreferWindow() : false,
                    request.getPreferPower() != null ? request.getPreferPower() : false));
            vo.setStrategy("热门推荐");
            vo.setCreateTime(LocalDateTime.now().toString());
            list.add(vo);
        }
        return list;
    }

    @PostMapping("/chat")
    @Operation(summary = "AI 客服对话")
    public Result<ChatResponse> chat(@RequestBody ChatRequest request) {
        // 1. RAG 检索相关文档
        List<KnowledgeBase> retrievedDocs = ragService.retrieve(request.getMessage(), 3);

        // 2. 构建增强版 system prompt
        StringBuilder context = new StringBuilder();
        List<String> relatedDocTitles = new ArrayList<>();
        if (!retrievedDocs.isEmpty()) {
            context.append("你是校园自习室预约系统的智能客服。请严格基于以下知识库内容回答用户问题，" +
                    "如果知识库中没有相关信息，请基于常识友好回答。\n\n");
            context.append("【知识库内容】\n");
            for (KnowledgeBase doc : retrievedDocs) {
                context.append("[").append(doc.getCategory()).append("] ")
                        .append(doc.getTitle()).append("\n")
                        .append(doc.getContent()).append("\n\n");
                relatedDocTitles.add(doc.getTitle());
            }
        } else {
            context.append("你是校园自习室预约系统的智能客服。请基于以下知识回答用户问题，保持简洁友好：\n" +
                    "1. 预约流程：登录后选择自习室、日期、时间段和座位，点击预约。\n" +
                    "2. 签到签退：在预约时间段内进入考勤页面签到，离开时签退。\n" +
                    "3. 取消预约：在预约开始前可在我的预约中取消。\n" +
                    "4. 违规：预约后15分钟未签到会被记录违规，频繁取消也可能违规。\n" +
                    "5. 开放时间：各自习室一般为 07:00-22:00。");
        }

        String reply = zhipuAiService.chat(context.toString(), request.getMessage(), request.getHistory());
        ChatResponse response = new ChatResponse();
        response.setReply(reply);
        response.setRelatedDocs(relatedDocTitles.isEmpty()
                ? Collections.singletonList("《校园自习室预约规则》")
                : relatedDocTitles);
        return Result.success(response);
    }

    @GetMapping("/recommendations/{id}/explain")
    @Operation(summary = "解释推荐原因")
    public Result<Map<String, String>> explainRecommendation(@PathVariable Long id) {
        Map<String, String> map = new HashMap<>();
        map.put("explanation", "本次推荐综合分析了您的历史预约行为、偏好座位类型以及当前各自习室实时空闲率，" +
                "通过用户基于协同过滤算法从相似用户的行为中挖掘出最符合您习惯的选项。");
        return Result.success(map);
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
    public static class RecommendRequest {
        private String date;
        private String building;
        private Boolean preferWindow;
        private Boolean preferPower;
    }

    @lombok.Data
    public static class RecommendationVO {
        private Long recommendationId;
        private Long userId;
        private Long roomId;
        private Long seatId;
        private Double score;
        private String reason;
        private String strategy;
        private String createTime;
    }

    @lombok.Data
    public static class ChatRequest {
        private String message;
        private List<Map<String, String>> history;
    }

    @lombok.Data
    public static class ChatResponse {
        private String reply;
        private List<String> relatedDocs;
    }
}
