package com.campus.ai.service;

import com.campus.ai.controller.AIController;
import com.campus.ai.entity.ReservationRecord;
import com.campus.ai.mapper.ReservationRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollaborativeFilterService {

    private final ReservationRecordMapper reservationRecordMapper;

    /**
     * 用户基于协同过滤推荐房间
     */
    public List<Recommendation> recommendRooms(Long userId, int topN) {
        List<ReservationRecord> allRecords = reservationRecordMapper.selectAllActiveRecords();
        if (allRecords.isEmpty()) {
            return Collections.emptyList();
        }

        // 构建用户-房间偏好矩阵（按房间聚合）
        Map<Long, Map<Long, Integer>> userRoomMatrix = buildUserRoomMatrix(allRecords);
        Map<Long, Integer> targetVector = userRoomMatrix.getOrDefault(userId, Collections.emptyMap());

        // 冷启动：目标用户没有历史记录
        if (targetVector == null || targetVector.isEmpty()) {
            log.info("用户 {} 无预约历史，返回空列表由内容推荐兜底", userId);
            return Collections.emptyList();
        }

        // 计算与其他用户的余弦相似度
        Map<Long, Double> similarities = new HashMap<>();
        for (Map.Entry<Long, Map<Long, Integer>> entry : userRoomMatrix.entrySet()) {
            Long otherUserId = entry.getKey();
            if (otherUserId.equals(userId)) {
                continue;
            }
            double sim = cosineSimilarity(targetVector, entry.getValue());
            if (sim > 0) {
                similarities.put(otherUserId, sim);
            }
        }

        // 取 Top-10 相似用户
        List<Long> similarUsers = similarities.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (similarUsers.isEmpty()) {
            return Collections.emptyList();
        }

        // 聚合相似用户的房间偏好，排除目标用户已去过的房间
        Map<Long, Double> roomScores = new HashMap<>();
        for (Long similarUserId : similarUsers) {
            double sim = similarities.get(similarUserId);
            Map<Long, Integer> preferences = userRoomMatrix.get(similarUserId);
            if (preferences == null) {
                continue;
            }
            for (Map.Entry<Long, Integer> entry : preferences.entrySet()) {
                Long roomId = entry.getKey();
                if (targetVector.containsKey(roomId)) {
                    continue; // 排除已去过的房间
                }
                roomScores.merge(roomId, sim * entry.getValue(), Double::sum);
            }
        }

        // 归一化并返回 TopN
        double totalSim = similarUsers.stream().mapToDouble(similarities::get).sum();
        return roomScores.entrySet().stream()
                .map(e -> new Recommendation(e.getKey(), totalSim > 0 ? e.getValue() / totalSim : e.getValue()))
                .sorted((a, b) -> Double.compare(b.score(), a.score()))
                .limit(topN)
                .collect(Collectors.toList());
    }

    /**
     * 内容推荐：根据用户偏好（教学楼、靠窗、电源）推荐房间
     */
    public List<Recommendation> contentBasedRecommend(Long userId,
                                                          String preferredBuilding,
                                                          Boolean preferWindow,
                                                          Boolean preferPower,
                                                          int topN) {
        // 简单规则：优先推荐用户没去过且符合偏好的热门房间
        List<ReservationRecord> allRecords = reservationRecordMapper.selectAllActiveRecords();
        Map<Long, Integer> userRooms = reservationRecordMapper.selectByUserId(userId).stream()
                .collect(Collectors.toMap(ReservationRecord::getRoomId, ReservationRecord::getCount,
                        Integer::sum, HashMap::new));

        // 统计各房间总预约次数
        Map<Long, Integer> roomPopularity = new HashMap<>();
        for (ReservationRecord record : allRecords) {
            roomPopularity.merge(record.getRoomId(), record.getCount(), Integer::sum);
        }

        // 获取房间最大预约次数，用于归一化
        int maxPopularity = roomPopularity.values().stream().max(Integer::compareTo).orElse(1);

        return roomPopularity.entrySet().stream()
                .filter(e -> !userRooms.containsKey(e.getKey())) // 排除已去过的
                .map(e -> {
                    double baseScore = maxPopularity > 0 ? e.getValue().doubleValue() / maxPopularity : e.getValue().doubleValue();
                    // 暂时按 popularity 评分，教学楼/座位偏好在 Controller 选座位时体现
                    return new Recommendation(e.getKey(), baseScore);
                })
                .sorted((a, b) -> Double.compare(b.score(), a.score()))
                .limit(topN)
                .collect(Collectors.toList());
    }

    private Map<Long, Map<Long, Integer>> buildUserRoomMatrix(List<ReservationRecord> records) {
        Map<Long, Map<Long, Integer>> matrix = new HashMap<>();
        for (ReservationRecord record : records) {
            matrix.computeIfAbsent(record.getUserId(), k -> new HashMap<>())
                    .merge(record.getRoomId(), record.getCount(), Integer::sum);
        }
        return matrix;
    }

    private double cosineSimilarity(Map<Long, Integer> v1, Map<Long, Integer> v2) {
        double dot = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        Set<Long> allKeys = new HashSet<>(v1.keySet());
        allKeys.addAll(v2.keySet());

        for (Long key : allKeys) {
            int a = v1.getOrDefault(key, 0);
            int b = v2.getOrDefault(key, 0);
            dot += a * b;
            norm1 += a * a;
            norm2 += b * b;
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }
        return dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    public record Recommendation(Long roomId, double score) {
    }
}
