package com.campus.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ZhipuAiService {

    @Value("${ai.zhipu.base-url:https://open.bigmodel.cn/api/paas/v4}")
    private String baseUrl;

    @Value("${ai.zhipu.api-key:}")
    private String apiKey;

    @Value("${ai.zhipu.model:glm-4}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 通用对话接口
     */
    public String chat(String systemPrompt, String userMessage, List<Map<String, String>> history) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("智谱 API Key 未配置，使用本地知识库回答");
            return localChatAnswer(userMessage);
        }

        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", model);

            ArrayNode messages = requestBody.putArray("messages");
            if (systemPrompt != null && !systemPrompt.isBlank()) {
                ObjectNode systemMsg = messages.addObject();
                systemMsg.put("role", "system");
                systemMsg.put("content", systemPrompt);
            }

            if (history != null) {
                for (Map<String, String> h : history) {
                    ObjectNode msg = messages.addObject();
                    msg.put("role", h.getOrDefault("role", "user"));
                    msg.put("content", h.getOrDefault("content", ""));
                }
            }

            ObjectNode userMsg = messages.addObject();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/chat/completions", entity, String.class);

            return extractContent(response.getBody());
        } catch (Exception e) {
            log.error("调用智谱 AI 失败", e);
            return localChatAnswer(userMessage);
        }
    }

    /**
     * 生成推荐理由
     */
    public String generateRecommendationReason(String building, String seatType, boolean hasPower, boolean preferWindow, boolean preferPower) {
        if (apiKey == null || apiKey.isBlank()) {
            return localRecommendationReason(building, seatType, hasPower, preferWindow, preferPower);
        }

        String prompt = String.format(
                "你是一位校园自习室推荐助手。请根据以下信息，用一句话生成温馨的推荐理由（不超过60字）：\n" +
                        "教学楼：%s，座位类型：%s，%s，%s。",
                building,
                seatType,
                hasPower ? "有电源" : "无电源",
                preferWindow ? "用户喜欢靠窗" : (preferPower ? "用户需要电源" : "用户无特殊偏好")
        );
        return chat("你是一位校园自习室推荐助手，回答简洁、友好。", prompt, null);
    }

    /**
     * 本地知识库回答（无 API Key 或调用失败时使用）
     */
    private String localChatAnswer(String message) {
        String lower = message.toLowerCase();
        if (contains(lower, "预约", "怎么约", "如何预约", "预订")) {
            return "预约流程很简单：登录后进入【自习室】页面，选择日期和时间段，点击蓝色可选座位，再点\"立即预约\"即可。";
        }
        if (contains(lower, "签到", "怎么签到", "打卡")) {
            return "在预约时间段内，进入【考勤记录】页面，找到对应预约，点击\"签到\"按钮即可。离开时再点击\"签退\"。";
        }
        if (contains(lower, "取消", "退订", "不约了")) {
            return "在预约开始之前，进入【我的预约】页面，点击对应预约的\"取消\"按钮即可取消。";
        }
        if (contains(lower, "迟到", "没来", "违规", "惩罚", "后果")) {
            return "预约后 15 分钟内未签到会被记录为违规；频繁取消预约也可能被记录违规，影响后续预约权限哦。";
        }
        if (contains(lower, "开放", "几点", "时间", "营业")) {
            return "各自习室一般开放时间为 07:00-22:00，具体以各楼公告为准。";
        }
        if (contains(lower, "电源", "插座", "充电")) {
            return "带有\"电源\"标签的座位配备插座，适合需要给电脑、手机充电的同学。";
        }
        if (contains(lower, "靠窗", "窗户", "安静")) {
            return "靠窗座位光线较好，角落座位相对安静，您可以根据学习习惯在筛选条件中选择偏好。";
        }
        if (contains(lower, "推荐", "智能推荐", "ai推荐")) {
            return "您可以进入【AI 智能推荐】页面，选择日期、教学楼和座位偏好，系统会为您推荐合适的自习室和座位。";
        }
        if (contains(lower, "你好", "您好", "hi", "hello")) {
            return "您好！我是校园自习室智能客服，请问有什么可以帮您？";
        }
        if (contains(lower, "谢谢", "感谢")) {
            return "不客气！祝您学习愉快~ 📚";
        }
        return "抱歉，我暂时无法回答这个问题。您可以尝试咨询\"如何预约自习室\"、\"怎么签到\"、\"取消预约\"等常见问题。";
    }

    private boolean contains(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 本地推荐理由生成（无 API Key 或调用失败时使用）
     */
    private String localRecommendationReason(String building, String seatType, boolean hasPower, boolean preferWindow, boolean preferPower) {
        StringBuilder reason = new StringBuilder();
        reason.append("位于").append(building).append("，");

        switch (seatType.toLowerCase()) {
            case "window" -> reason.append("靠窗采光好");
            case "corner" -> reason.append("角落位置安静");
            case "power" -> reason.append("配有电源插座");
            default -> reason.append("布局宽敞舒适");
        }

        if (hasPower && !"power".equalsIgnoreCase(seatType)) {
            reason.append("且可充电");
        }

        if (preferWindow && !"window".equalsIgnoreCase(seatType)) {
            reason.append("，尽量满足您靠窗的偏好");
        } else if (preferPower && !hasPower && !"power".equalsIgnoreCase(seatType)) {
            reason.append("，但当前房间电源座位已满");
        } else if (preferPower && (hasPower || "power".equalsIgnoreCase(seatType))) {
            reason.append("，满足您需要电源的需求");
        }

        reason.append("。");
        return reason.toString();
    }

    private String extractContent(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                return choices.get(0).path("message").path("content").asText("AI 未能生成回答");
            }
            return "AI 未能生成回答";
        } catch (Exception e) {
            log.error("解析智谱 AI 响应失败: {}", responseBody, e);
            return "AI 响应解析失败";
        }
    }
}
