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

        String requestJson = null;
        String responseBody = null;
        String outputText = null;
        try {
            requestJson = objectMapper.writeValueAsString(body);
            log.debug("AI request (first 300 chars): {}",
                    requestJson.length() > 300 ? requestJson.substring(0, 300) + "..." : requestJson);
            responseBody = executeWithNetworkRetry(requestJson);
            log.debug("AI response (first 500 chars): {}",
                    responseBody.length() > 500 ? responseBody.substring(0, 500) + "..." : responseBody);
            JsonNode response = objectMapper.readTree(responseBody);
            outputText = extractOutputText(response);
            log.debug("AI extracted output (first 500 chars): {}",
                    outputText.length() > 500 ? outputText.substring(0, 500) + "..." : outputText);
            return objectMapper.readValue(outputText, responseType);
        } catch (JsonProcessingException e) {
            // 模型可能返回带markdown包裹的JSON或纯文本，尝试提取JSON后重新解析
            if (outputText != null) {
                String extractedJson = extractJsonFromText(outputText);
                if (extractedJson != null) {
                    try {
                        log.info("从文本中提取到JSON，重新解析");
                        return objectMapper.readValue(extractedJson, responseType);
                    } catch (JsonProcessingException ignored) {
                        // 提取的JSON仍然解析失败，继续走fallback
                    }
                }
                if (!outputText.trim().startsWith("{")) {
                    log.warn("AI返回纯文本而非JSON，尝试fallback包装为 {}: text前100字={}",
                            responseType.getSimpleName(),
                            outputText.length() > 100 ? outputText.substring(0, 100) + "..." : outputText);
                    return wrapPlainTextFallback(outputText.trim(), responseType);
                }
            }
            log.warn("AI JSON解析失败: {} — location={}, outputText={}",
                    e.getMessage(), e.getLocation(),
                    outputText != null ? outputText.substring(0, Math.min(500, outputText.length())) : "null");
            throw new BusinessException(502, "AI 模型返回结果解析失败");
        } catch (BusinessException e) {
            log.warn("AI 业务异常(code={}): {} — responseBody={}",
                    e.getCode(), e.getMessage(),
                    responseBody != null ? responseBody.substring(0, Math.min(500, responseBody.length())) : "null");
            throw e;
        }
    }

    /**
     * qwen模型常忽略json_schema直接返回自然语言。
     * 智能填充响应DTO：首个String字段填纯文本，同时尝试从文本中提取结构化信息。
     */
    private <T> T wrapPlainTextFallback(String plainText, Class<T> responseType) {
        try {
            T instance = responseType.getDeclaredConstructor().newInstance();

            // 特殊处理：AiQuestionResponse — 从纯文本中按编号拆分为多个Question
            if (responseType.getSimpleName().equals("AiQuestionResponse")) {
                return parseQuestionResponseFromPlainText(plainText, responseType);
            }

            java.util.List<String> extractedQuestions = extractSuggestedQuestions(plainText);
            String mainText = stripSuggestedQuestions(plainText);

            for (var field : responseType.getDeclaredFields()) {
                if (field.getType() == String.class) {
                    // 跳过 id/sessionId 等非内容字段，只填内容字段
                    String name = field.getName().toLowerCase();
                    if (name.contains("id") || name.contains("time") || name.contains("duration")
                            || name.contains("score") || name.contains("title")) {
                        continue;
                    }
                    field.setAccessible(true);
                    field.set(instance, mainText);
                } else if (field.getType() == java.util.List.class && !extractedQuestions.isEmpty()) {
                    field.setAccessible(true);
                    field.set(instance, extractedQuestions);
                }
            }
            return instance;
        } catch (Exception ex) {
            throw new BusinessException(502, "AI 模型返回了纯文本，且无法自动包装为 " + responseType.getSimpleName());
        }
    }

    /**
     * 从纯文本中按编号拆分面试题，构造 AiQuestionResponse。
     * 文本格式：1. 题目1  2. 题目2  ...  或  Q1: 题目1  Q2: 题目2  ...
     */
    @SuppressWarnings("unchecked")
    private <T> T parseQuestionResponseFromPlainText(String plainText, Class<T> responseType) {
        java.util.List<Object> questions = new java.util.ArrayList<>();
        // 按 "数字. " 或 "数字) " 或 "Q数字: " 或 "Q数字. " 拆分
        String[] parts = plainText.split("\\n(?=(?:\\d+[.)]\\s|Q\\d+[:.]))");
        if (parts.length <= 1) {
            // 没有编号，整个文本作为一道题
            parts = new String[] { plainText };
        }
        for (String part : parts) {
            String text = part.trim();
            if (text.isEmpty()) continue;
            // 去除前导编号 "1. " "Q1: " 等
            text = text.replaceFirst("^(?:Q?\\d+[.):]\\s*)+", "").trim();
            if (text.isEmpty()) continue;
            try {
                // 反射创建 Question 内部类实例
                Class<?> questionClass = Class.forName(
                        "com.recruitment.api.dto.AiQuestionResponse$Question");
                Object question = questionClass.getDeclaredConstructor().newInstance();
                questionClass.getMethod("setTitle", String.class).invoke(question, text);
                questionClass.getMethod("setDifficulty", String.class).invoke(question, "中等");
                questions.add(question);
            } catch (Exception ignored) {
                // 反射创建失败则跳过
            }
        }
        try {
            T instance = responseType.getDeclaredConstructor().newInstance();
            responseType.getMethod("setQuestions", java.util.List.class).invoke(instance, questions);
            log.info("从纯文本中解析出 {} 道面试题", questions.size());
            return instance;
        } catch (Exception e) {
            throw new BusinessException(502, "AI 返回纯文本且无法解析为面试题");
        }
    }

    private java.util.List<String> extractSuggestedQuestions(String text) {
        java.util.List<String> questions = new java.util.ArrayList<>();
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(
                "(?:建议反问|反问建议)[】：:]\\s*(.+?)(?:$|\\Z)",
                java.util.regex.Pattern.DOTALL).matcher(text);
        if (m.find()) {
            String section = m.group(1);
            java.util.regex.Matcher itemMatcher = java.util.regex.Pattern.compile(
                    "\\d+[.、)）]\\s*(.+?)(?=(?:\\d+[.、)）])|$)", java.util.regex.Pattern.DOTALL).matcher(section);
            while (itemMatcher.find()) {
                String q = itemMatcher.group(1).trim();
                if (!q.isEmpty()) {
                    questions.add(q);
                }
            }
        }
        return questions;
    }

    private String stripSuggestedQuestions(String text) {
        return text.replaceAll("\\s*【?建议反问】?[：:]\\s*.+$", "").replaceAll("\\s*【?反问建议】?[：:]\\s*.+$", "");
    }

    /**
     * 从可能包含markdown代码块或前后多余文本的内容中提取JSON字符串。
     * 支持 ```json {...} ``` 和 ``` {...} ``` 以及纯文本中嵌入的 {...} 格式。
     */
    private String extractJsonFromText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String trimmed = text.trim();

        // 情况1：```json\n{...}\n``` 或 ```\n{...}\n```
        java.util.regex.Matcher codeBlockMatcher = java.util.regex.Pattern.compile(
                "```(?:json)?\\s*\\n?(\\{.*?})\\s*```",
                java.util.regex.Pattern.DOTALL).matcher(trimmed);
        if (codeBlockMatcher.find()) {
            return codeBlockMatcher.group(1).trim();
        }

        // 情况2：文本中直接包含 {...}（取第一个完整的JSON对象）
        int braceStart = trimmed.indexOf('{');
        if (braceStart >= 0) {
            int depth = 0;
            boolean inString = false;
            boolean escaped = false;
            for (int i = braceStart; i < trimmed.length(); i++) {
                char c = trimmed.charAt(i);
                if (escaped) {
                    escaped = false;
                    continue;
                }
                if (c == '\\') {
                    escaped = true;
                    continue;
                }
                if (c == '"') {
                    inString = !inString;
                    continue;
                }
                if (inString) continue;
                if (c == '{') depth++;
                else if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        return trimmed.substring(braceStart, i + 1);
                    }
                }
            }
        }

        return null;
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
