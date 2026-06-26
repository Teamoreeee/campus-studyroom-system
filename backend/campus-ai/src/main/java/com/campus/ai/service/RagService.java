package com.campus.ai.service;

import com.campus.ai.entity.KnowledgeBase;
import com.campus.ai.mapper.KnowledgeBaseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RagService {

    private final KnowledgeBaseMapper knowledgeBaseMapper;

    /**
     * 基于关键词的轻量级 RAG 检索
     */
    public List<KnowledgeBase> retrieve(String query, int topK) {
        List<String> tokens = tokenize(query);
        if (tokens.isEmpty()) {
            return Collections.emptyList();
        }

        List<KnowledgeBase> allDocs = knowledgeBaseMapper.selectActiveList();
        Map<KnowledgeBase, Double> scores = new HashMap<>();

        for (KnowledgeBase doc : allDocs) {
            double score = calculateScore(tokens, doc);
            if (score > 0) {
                scores.put(doc, score);
            }
        }

        return scores.entrySet().stream()
                .sorted(Map.Entry.<KnowledgeBase, Double>comparingByValue().reversed())
                .limit(topK)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private double calculateScore(List<String> tokens, KnowledgeBase doc) {
        double score = 0.0;
        String title = doc.getTitle() == null ? "" : doc.getTitle().toLowerCase();
        String content = doc.getContent() == null ? "" : doc.getContent().toLowerCase();
        String keywords = doc.getKeywords() == null ? "" : doc.getKeywords().toLowerCase();

        for (String token : tokens) {
            if (title.contains(token)) {
                score += 3.0;
            }
            if (keywords.contains(token)) {
                score += 2.0;
            }
            if (content.contains(token)) {
                score += 1.0;
            }
        }

        // 浏览量作为辅助排序因子
        score += Math.log(Math.max(doc.getViewCount(), 1) + 1) * 0.1;
        return score;
    }

    private List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        // 中文按字符切分 + 英文/数字按空格标点切分
        String lower = text.toLowerCase();
        List<String> tokens = new ArrayList<>();

        // 先按空格和标点拆分
        String[] parts = lower.split("[\\s,，.。!！?？;；:：\"\"'']");
        for (String part : parts) {
            if (part.length() >= 2) {
                tokens.add(part);
            }
        }

        // 对中文文本再按 2-gram 提取
        for (String part : parts) {
            if (part.matches(".*[\\u4e00-\\u9fa5].*")) {
                for (int i = 0; i < part.length() - 1; i++) {
                    tokens.add(part.substring(i, i + 2));
                }
            }
        }

        return tokens.stream().distinct().collect(Collectors.toList());
    }
}
