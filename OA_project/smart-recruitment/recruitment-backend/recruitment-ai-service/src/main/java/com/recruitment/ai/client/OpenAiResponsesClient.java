package com.recruitment.ai.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.ai.config.OpenAiProperties;
import com.recruitment.common.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenAiResponsesClient {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient openAiHttpClient;
    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;

    public <T> T generateStructured(String instructions, String input, String schemaName,
                                    Map<String, Object> schema, Class<T> responseType) {
        validateConfiguration();

        Map<String, Object> format = new LinkedHashMap<>();
        format.put("type", "json_schema");
        format.put("name", schemaName);
        format.put("strict", true);
        format.put("schema", schema);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", properties.getModel());
        body.put("instructions", instructions);
        body.put("input", input);
        body.put("store", false);
        body.put("text", Map.of("format", format));
        if (properties.getMaxOutputTokens() > 0) {
            body.put("max_output_tokens", properties.getMaxOutputTokens());
        }
        if (StringUtils.hasText(properties.getReasoningEffort())) {
            body.put("reasoning", Map.of("effort", properties.getReasoningEffort().trim()));
        }

        try {
            String responseBody = executeWithNetworkRetry(objectMapper.writeValueAsString(body));
            JsonNode response = objectMapper.readTree(responseBody);
            String outputText = extractOutputText(response);
            return objectMapper.readValue(outputText, responseType);
        } catch (JsonProcessingException e) {
            throw new BusinessException(502, "AI 模型返回结果解析失败");
        }
    }

    private String executeWithNetworkRetry(String requestJson) {
        IOException lastException = null;
        String responsesUrl = properties.getBaseUrl().replaceAll("/+$", "") + "/v1/responses";
        Request request = new Request.Builder()
                .url(responsesUrl)
                .header("Authorization", "Bearer " + properties.getApiKey().trim())
                .header("Accept", "application/json")
                .header("User-Agent", "SmartRecruitmentAI/1.0")
                .post(RequestBody.create(requestJson, JSON))
                .build();

        for (int attempt = 1; attempt <= 3; attempt++) {
            try (Response response = openAiHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new BusinessException(502, "AI 模型调用失败（HTTP " + response.code() + "）");
                }
                ResponseBody body = response.body();
                if (body == null) {
                    throw new BusinessException(502, "AI 模型未返回响应内容");
                }
                return body.string();
            } catch (IOException e) {
                lastException = e;
                if (attempt < 3) {
                    waitBeforeRetry(attempt);
                }
            }
        }
        log.warn("AI model connection failed after retries: {}",
                lastException == null ? "unknown I/O error" : lastException.getMessage());
        throw new BusinessException(503, "AI 模型连接超时或服务不可用");
    }

    private void waitBeforeRetry(int attempt) {
        try {
            Thread.sleep(500L * attempt);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(503, "AI 模型调用已中断");
        }
    }

    private void validateConfiguration() {
        if (!StringUtils.hasText(properties.getApiKey())
                || properties.getApiKey().contains("placeholder")
                || properties.getApiKey().contains("replace-with")) {
            throw new BusinessException(503,
                    "未配置 OPENAI_API_KEY，请填写 recruitment-ai-service/application-secrets.properties");
        }
    }

    private String extractOutputText(JsonNode response) {
        if (response == null || !response.path("output").isArray()) {
            throw new BusinessException(502, "AI 模型未返回有效内容");
        }

        StringBuilder text = new StringBuilder();
        for (JsonNode outputItem : response.path("output")) {
            for (JsonNode contentItem : outputItem.path("content")) {
                if ("refusal".equals(contentItem.path("type").asText())) {
                    throw new BusinessException(422, "AI 模型拒绝处理当前内容");
                }
                if ("output_text".equals(contentItem.path("type").asText())) {
                    text.append(contentItem.path("text").asText());
                }
            }
        }
        if (text.isEmpty()) {
            throw new BusinessException(502, "AI 模型未返回文本结果");
        }
        return text.toString();
    }
}
