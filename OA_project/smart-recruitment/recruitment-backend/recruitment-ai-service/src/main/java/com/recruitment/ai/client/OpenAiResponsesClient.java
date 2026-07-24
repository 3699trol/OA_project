package com.recruitment.ai.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitment.ai.config.OpenAiProperties;
import com.recruitment.api.dto.AiQuestionResponse;
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
            T result = objectMapper.readValue(outputText, responseType);
            // qwen 模型常使用非标准字段名（如 "question" 而非 "title"），
            // Jackson 虽解析成功但字段为空。此时用灵活解析器重新解析补全字段。
            if (result instanceof AiQuestionResponse aiResp && hasMissingTitles(aiResp)) {
                T flexResult = tryParseFlexibleJson(outputText, responseType);
                if (flexResult != null) {
                    AiQuestionResponse flexResp = (AiQuestionResponse) flexResult;
                    if (flexResp.getQuestions() != null && !flexResp.getQuestions().isEmpty()
                            && flexResp.getQuestions().stream().anyMatch(q -> q.getTitle() != null && !q.getTitle().isEmpty())) {
                        log.info("标准解析title为空，灵活解析器补全（{} 道题）", flexResp.getQuestions().size());
                        return flexResult;
                    }
                }
            }
            return result;
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
                // JSON解析失败时，无论文本是否以 { 开头，都尝试从纯文本中提取结构化数据
                log.warn("AI JSON解析失败，尝试从文本中提取: text前200字={}",
                        outputText.length() > 200 ? outputText.substring(0, 200) + "..." : outputText);
                return wrapPlainTextFallback(outputText.trim(), responseType);
            }
            log.warn("AI 返回内容为空");
            throw new BusinessException(502, "AI 模型未返回有效内容");
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

            // 特殊处理：AiQuestionResponse — 先尝试JSON，再尝试从纯文本中拆分
            if (responseType.getSimpleName().equals("AiQuestionResponse")) {
                T jsonResult = tryParseFlexibleJson(plainText, responseType);
                if (jsonResult != null) return jsonResult;
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
     * 检查 AiQuestionResponse 中是否有题目的 title 缺失（qwen 模型返回 "question" 而非 "title" 时会出现）。
     */
    private boolean hasMissingTitles(AiQuestionResponse aiResp) {
        if (aiResp.getQuestions() == null || aiResp.getQuestions().isEmpty()) {
            return true;
        }
        return aiResp.getQuestions().stream()
                .anyMatch(q -> q.getTitle() == null || q.getTitle().isEmpty());
    }

    /**
     * 灵活尝试将文本解析为JSON（兼容字段名差异），成功返回AiQuestionResponse，失败返回null。
     */
    @SuppressWarnings("unchecked")
    private <T> T tryParseFlexibleJson(String text, Class<T> responseType) {
        try {
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(text);
            // 找到 questions 数组：可能在根节点，也可能被 key 包裹
            com.fasterxml.jackson.databind.JsonNode questionsNode = null;
            if (root.has("questions")) {
                questionsNode = root.get("questions");
            } else if (root.isArray()) {
                questionsNode = root;
            }
            // 遍历所有字段，找第一个数组类型的字段
            if (questionsNode == null) {
                var fields = root.fields();
                while (fields.hasNext()) {
                    var entry = fields.next();
                    if (entry.getValue().isArray()) {
                        questionsNode = entry.getValue();
                        break;
                    }
                }
            }
            if (questionsNode == null || !questionsNode.isArray() || questionsNode.size() == 0) {
                return null;
            }
            Class<?> questionClass = Class.forName(
                    "com.recruitment.api.dto.AiQuestionResponse$Question");
            java.util.List<Object> questions = new java.util.ArrayList<>();
            for (com.fasterxml.jackson.databind.JsonNode qNode : questionsNode) {
                Object q = questionClass.getDeclaredConstructor().newInstance();
                // 从 JSON 节点中取字段，兼容多种命名方式
                String title = firstJsonText(qNode, "title", "questionTitle", "question", "topic", "content", "name");
                String type = firstJsonText(qNode, "type", "questionType", "category", "kind");
                String difficulty = firstJsonText(qNode, "difficulty", "level", "difficultyLevel");
                String referenceAnswer = firstJsonText(qNode, "referenceAnswer", "answer", "reference", "solution", "modelAnswer");
                String scoringCriteria = firstJsonText(qNode, "scoringCriteria", "criteria", "scoring", "evaluationCriteria");

                questionClass.getMethod("setTitle", String.class).invoke(q,
                        title != null ? title : qNode.toString());
                questionClass.getMethod("setType", String.class).invoke(q, type);
                questionClass.getMethod("setDifficulty", String.class).invoke(q,
                        difficulty != null ? difficulty : "中等");
                questionClass.getMethod("setReferenceAnswer", String.class).invoke(q, referenceAnswer);
                questionClass.getMethod("setScoringCriteria", String.class).invoke(q, scoringCriteria);
                questions.add(q);
            }
            T instance = responseType.getDeclaredConstructor().newInstance();
            responseType.getMethod("setQuestions", java.util.List.class).invoke(instance, questions);
            log.info("从灵活JSON中解析出 {} 道面试题", questions.size());
            return instance;
        } catch (Exception e) {
            log.debug("灵活JSON解析失败: {}", e.getMessage());
            return null;
        }
    }

    /** 从 JSON 节点中取第一个非空的文本字段值，按候选名依次尝试。 */
    private String firstJsonText(com.fasterxml.jackson.databind.JsonNode node, String... candidates) {
        for (String name : candidates) {
            if (node.has(name) && !node.get(name).isNull()) {
                JsonNode valNode = node.get(name);
                // 如果是数组，将元素用换行符拼接
                if (valNode.isArray()) {
                    StringBuilder sb = new StringBuilder();
                    for (JsonNode item : valNode) {
                        if (sb.length() > 0) sb.append("\n");
                        sb.append(item.asText());
                    }
                    String val = sb.toString().trim();
                    if (!val.isEmpty()) return val;
                } else {
                    String val = valNode.asText().trim();
                    if (!val.isEmpty()) return val;
                }
            }
        }
        return null;
    }

    /**
     * 从纯文本中按编号拆分面试题，构造 AiQuestionResponse。
     * 文本格式：1. 题目  2. 题目  ...  或  Q1: 题目1  Q2: 题目2  ...
     * 每道题内可能包含子字段标记：类型: / 难度: / 参考答案: / 评分标准:
     */
    @SuppressWarnings("unchecked")
    private <T> T parseQuestionResponseFromPlainText(String plainText, Class<T> responseType) {
        java.util.List<Object> questions = new java.util.ArrayList<>();
        // 按 "数字. " 或 "数字) " 或 "Q数字: " 或 "Q数字. " 或 "**数字**" 拆分（在换行处）
        String[] parts = plainText.split("\\n(?=(?:(?:\\*\\*)?\\d+(?:\\*\\*)?[.)、]\\s*|Q\\d+[:.]))");
        if (parts.length <= 1) {
            parts = new String[] { plainText };
        }
        try {
            Class<?> questionClass = Class.forName(
                    "com.recruitment.api.dto.AiQuestionResponse$Question");
            for (String part : parts) {
                String text = part.trim();
                if (text.isEmpty()) continue;
                // 去除前导编号 "1. " "**1**、" "Q1: " 等
                text = text.replaceFirst("^(?:\\*\\*)?Q?\\d+(?:\\*\\*)?[.)、:]\\s*", "").trim();
                if (text.isEmpty()) continue;

                Object question = questionClass.getDeclaredConstructor().newInstance();

                // 提取子字段：类型、难度、参考答案、评分标准
                String title = extractTitle(text);
                String type = extractField(text, "类型[：:]", "题[型类][：:]");
                String difficulty = extractField(text, "难度[：:]", null);
                String referenceAnswer = extractField(text, "参考答案[：:]", "参考[答案解答][：:]");
                String scoringCriteria = extractField(text, "评分标准[：:]", "评[分判]标[准][：:]");

                questionClass.getMethod("setTitle", String.class).invoke(question, title);
                questionClass.getMethod("setType", String.class).invoke(question, type);
                questionClass.getMethod("setDifficulty", String.class).invoke(question,
                        difficulty != null ? difficulty : "中等");
                questionClass.getMethod("setReferenceAnswer", String.class).invoke(question, referenceAnswer);
                questionClass.getMethod("setScoringCriteria", String.class).invoke(question, scoringCriteria);
                questions.add(question);
            }
        } catch (Exception e) {
            log.warn("反射创建Question失败: {}", e.getMessage());
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

    /**
     * 提取题目正文（子字段标记之前的内容）。
     */
    private String extractTitle(String text) {
        // 去掉前导空行
        text = text.replaceFirst("^\\s*\\n+", "").trim();
        // 找到第一个子字段标记的位置，之前的内容为标题
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(
                "\\n\\s*(?:类型|题[型类]|难度|参考答案|参考[答案解答]|评分标准|评[分判]标[准])[：:]").matcher(text);
        if (m.find() && m.start() > 0) {
            String title = text.substring(0, m.start()).trim();
            title = title.replaceAll("[，。,.]$", "").trim();
            if (!title.isEmpty()) return title;
        }
        // 取第一个非空行作为标题
        for (String line : text.split("\\n")) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.matches("^(?:类型|题[型类]|难度|参考答案|参考[答案解答]|评分标准|评[分判]标[准])[：:].*")) {
                trimmed = trimmed.replaceAll("[，。,.]$", "").trim();
                if (!trimmed.isEmpty()) return trimmed;
            }
        }
        return "(题目)";
    }

    /**
     * 从文本中按正则匹配提取某个字段的值。
     * 提取从匹配位置到下一个子字段标记之间的内容。
     */
    private String extractField(String text, String... fieldPatterns) {
        for (String fieldPattern : fieldPatterns) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile(
                    "\\n\\s*" + fieldPattern + "\\s*",
                    java.util.regex.Pattern.CASE_INSENSITIVE).matcher(text);
            if (m.find()) {
                int valueStart = m.end();
                // 下一个子字段标记
                java.util.regex.Matcher nextField = java.util.regex.Pattern.compile(
                        "\\n\\s*(?:类型|题[型类]|难度|参考答案|参考[答案解答]|评分标准|评[分判]标[准])[：:]",
                        java.util.regex.Pattern.CASE_INSENSITIVE).matcher(text);
                int valueEnd = nextField.find(valueStart) ? nextField.start() : text.length();
                String value = text.substring(valueStart, valueEnd).trim();
                return value.isEmpty() ? null : value;
            }
        }
        return null;
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

        // 情况1：```json\n{...}\n``` 或 ```\n{...}\n``` 或 ```json\n[...]\n```
        java.util.regex.Matcher codeBlockMatcher = java.util.regex.Pattern.compile(
                "```(?:json)?\\s*\\n?((?:\\{.*?})|(?:\\[.*?]))\\s*```",
                java.util.regex.Pattern.DOTALL).matcher(trimmed);
        if (codeBlockMatcher.find()) {
            return codeBlockMatcher.group(1).trim();
        }

        // 情况2：文本中直接包含 {...} 或 [...]（取第一个完整的JSON对象/数组）
        int objStart = trimmed.indexOf('{');
        int arrStart = trimmed.indexOf('[');
        int braceStart = -1;
        char braceChar = 0;
        if (objStart >= 0 && arrStart >= 0) {
            if (objStart <= arrStart) { braceStart = objStart; braceChar = '{'; }
            else { braceStart = arrStart; braceChar = '['; }
        } else if (objStart >= 0) { braceStart = objStart; braceChar = '{'; }
        else if (arrStart >= 0) { braceStart = arrStart; braceChar = '['; }
        if (braceStart >= 0) {
            char closeBrace = braceChar == '{' ? '}' : ']';
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
                if (c == braceChar) depth++;
                else if (c == closeBrace) {
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
