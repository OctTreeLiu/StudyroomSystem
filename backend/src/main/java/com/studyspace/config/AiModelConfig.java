package com.studyspace.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiModelConfig {

    /**
     * OpenAI 兼容接口的 base-url（通常包含 /v1）
     */
    private String baseUrl;

    /**
     * API Key
     */
    private String apiKey;

    /**
     * 模型名
     */
    private String model;

    /**
     * 请求超时时间（秒）
     */
    private Integer timeoutSeconds = 30;

    /**
     * 是否启用联网搜索（DashScope compatible-mode 参数：enable_search）
     */
    private Boolean enableSearch = false;

    /**
     * 联网搜索策略（DashScope：search_options.search_strategy），例如 agent
     */
    private String searchStrategy = "agent";
}

