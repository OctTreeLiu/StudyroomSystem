package com.studyspace.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyspace.config.AiModelConfig;
import com.studyspace.dto.AiChatMessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * 大模型对话服务（OpenAI 兼容 Chat Completions）
 */
@Service
public class AiAgentService {

    private final HttpClient httpClient;

    @Autowired
    private AiModelConfig aiModelConfig;

    @Autowired
    private ObjectMapper objectMapper;

    public AiAgentService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String chat(List<AiChatMessageDTO> messages) throws Exception {
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("messages不能为空");
        }

        if (aiModelConfig.getApiKey() == null || aiModelConfig.getApiKey().trim().isEmpty()) {
            throw new IllegalStateException("未配置 AI apiKey，请在 application.yml 设置 ai.api-key 或环境变量 AI_API_KEY");
        }
        if (aiModelConfig.getBaseUrl() == null || aiModelConfig.getBaseUrl().trim().isEmpty()) {
            throw new IllegalStateException("未配置 AI base-url，请在 application.yml 设置 ai.base-url");
        }
        if (aiModelConfig.getModel() == null || aiModelConfig.getModel().trim().isEmpty()) {
            throw new IllegalStateException("未配置 AI model，请在 application.yml 设置 ai.model");
        }

        String baseUrl = aiModelConfig.getBaseUrl().trim();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        String url = baseUrl + "/chat/completions";

        // OpenAI 兼容请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", aiModelConfig.getModel());
        requestBody.put("messages", messages.stream().map(m -> {
            Map<String, String> map = new HashMap<>();
            map.put("role", m.getRole());
            map.put("content", m.getContent());
            return map;
        }).collect(toList()));
        requestBody.put("temperature", 0.2);

        // DashScope compatible-mode：可在 Chat Completions 中开启联网搜索
        // 说明：如果当前模型不支持 web search，可能会忽略或报错；请使用支持 web search 的 qwen3/qwen3.5 系列模型。
        boolean enableSearch = Boolean.TRUE.equals(aiModelConfig.getEnableSearch());
        if (enableSearch) {
            requestBody.put("enable_search", true);
            requestBody.put("search_options", Map.of(
                    "search_strategy", aiModelConfig.getSearchStrategy() == null ? "agent" : aiModelConfig.getSearchStrategy()
            ));
            // 思考模式 + 联网搜索在 DashScope 兼容接口下必须使用流式，否则会 400
            requestBody.put("stream", true);
        }

        String bodyJson = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + aiModelConfig.getApiKey().trim())
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(
                        aiModelConfig.getTimeoutSeconds() == null ? 30 : aiModelConfig.getTimeoutSeconds()
                ))
                .POST(HttpRequest.BodyPublishers.ofString(bodyJson))
                .build();

        if (enableSearch) {
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream in = response.body();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    String errBody = reader.lines().reduce("", (a, b) -> a.isEmpty() ? b : a + "\n" + b);
                    throw new RuntimeException("AI请求失败: HTTP " + response.statusCode() + ", body=" + errBody);
                }
                return aggregateStreamContent(reader);
            }
        }

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("AI请求失败: HTTP " + response.statusCode() + ", body=" + response.body());
        }

        // 解析 OpenAI 兼容响应
        // expected:
        // { choices: [ { message: { content: "..." } } ] }
        var root = objectMapper.readTree(response.body());
        var choices = root.path("choices");
        if (choices.isMissingNode() || !choices.isArray() || choices.size() == 0) {
            throw new RuntimeException("AI返回格式错误: 缺少 choices");
        }

        var contentNode = choices.get(0).path("message").path("content");
        if (contentNode.isMissingNode()) {
            throw new RuntimeException("AI返回格式错误: 缺少 message.content");
        }

        return contentNode.asText();
    }

    /**
     * 解析 OpenAI 兼容的 SSE（data: {...}），拼接 choices[0].delta.content。
     */
    private String aggregateStreamContent(BufferedReader reader) throws Exception {
        StringBuilder content = new StringBuilder();
        StringBuilder reasoning = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                continue;
            }
            if (!line.startsWith("data:")) {
                continue;
            }
            String data = line.startsWith("data: ") ? line.substring(6).trim() : line.substring(5).trim();
            if (data.isEmpty() || "[DONE]".equals(data)) {
                if ("[DONE]".equals(data)) {
                    break;
                }
                continue;
            }
            var root = objectMapper.readTree(data);
            if (root.has("error")) {
                var err = root.path("error");
                String msg = err.path("message").asText(err.toString());
                throw new RuntimeException("AI流式返回错误: " + msg);
            }
            var choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                continue;
            }
            var delta = choices.get(0).path("delta");
            var piece = delta.path("content");
            if (!piece.isMissingNode() && !piece.isNull()) {
                content.append(piece.asText());
            }
            var reasoningPiece = delta.path("reasoning_content");
            if (!reasoningPiece.isMissingNode() && !reasoningPiece.isNull()) {
                reasoning.append(reasoningPiece.asText());
            }
        }
        if (content.length() > 0) {
            return content.toString();
        }
        if (reasoning.length() > 0) {
            return reasoning.toString();
        }
        throw new RuntimeException("AI返回格式错误: 流式响应未解析到正文内容");
    }
}

