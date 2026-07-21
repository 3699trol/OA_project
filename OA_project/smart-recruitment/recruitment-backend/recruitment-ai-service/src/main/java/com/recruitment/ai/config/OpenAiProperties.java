package com.recruitment.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ai.openai")
public class OpenAiProperties {

    private String apiKey;
    private String baseUrl = "https://api.openai.com";
    private String model = "gpt-4o-mini";
    private String reasoningEffort;
    private String proxyHost;
    private int proxyPort;
    private int connectTimeoutSeconds = 10;
    private int readTimeoutSeconds = 90;
}
