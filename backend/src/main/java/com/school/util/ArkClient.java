package com.school.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.common.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 火山方舟（Ark）大模型客户端，模型 Doubao-Seed-2.1-pro
 * 密钥缺失或调用失败时可回退到本地模拟（保证演示可跑）
 */
@Slf4j
@Component
public class ArkClient {

    @Value("${ark.base-url}")
    private String baseUrl;

    @Value("${ark.endpoint-id}")
    private String endpointId;

    @Value("${ark.api-key}")
    private String apiKey;

    @Value("${ark.model}")
    private String model;

    @Value("${ark.temperature:0.7}")
    private double temperature;

    @Value("${ark.mock-fallback:true}")
    private boolean mockFallback;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 对话（非流式），返回助手文本内容
     */
    public String chat(String systemPrompt, String userPrompt) {
        try {
            if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("api-key-")) {
                return mockReply(systemPrompt, userPrompt);
            }
            String url = baseUrl + "/chat/completions";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            List<Map<String, String>> messages = new ArrayList<>();
            if (systemPrompt != null && !systemPrompt.isBlank()) {
                Map<String, String> sys = new HashMap<>();
                sys.put("role", "system");
                sys.put("content", systemPrompt);
                messages.add(sys);
            }
            Map<String, String> user = new HashMap<>();
            user.put("role", "user");
            user.put("content", userPrompt);
            messages.add(user);

            Map<String, Object> body = new HashMap<>();
            body.put("model", endpointId);
            body.put("messages", messages);
            body.put("stream", false);
            body.put("temperature", temperature);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> resp = restTemplate.postForEntity(url, entity, String.class);
            JsonNode root = objectMapper.readTree(resp.getBody());
            return root.path("choices").path(0).path("message").path("content").asText();
        } catch (Exception e) {
            log.warn("Ark 调用失败，回退模拟: {}", e.getMessage());
            if (mockFallback) {
                return mockReply(systemPrompt, userPrompt);
            }
            throw new BusinessException("AI 服务暂不可用，请稍后重试");
        }
    }

    /**
     * 本地模拟回复（无密钥/调用失败时兜底，保证演示流程可走通）
     */
    private String mockReply(String systemPrompt, String userPrompt) {
        StringBuilder sb = new StringBuilder();
        sb.append("【AI 学情分析（本地模拟）】\n");
        sb.append("1. 知识掌握：整体基础扎实，但「几何证明」与「函数图像」两个知识点得分率偏低，是本阶段主要薄弱环节。\n");
        sb.append("2. 错题归因：约 60% 的失分来自计算粗心与审题不清，建议加强限时训练与错题复盘。\n");
        sb.append("3. 学习建议：每日安排 20 分钟针对薄弱知识点专项练习，建立错题本，一周内重做同类题型巩固。\n");
        sb.append("（当前未配置有效的 Ark API Key，此内容为本地演示输出）");
        return sb.toString();
    }
}
