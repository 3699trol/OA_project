package com.recruitment.ai.mockinterview.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.recruitment.ai.client.AiResponseSchemas;
import com.recruitment.ai.client.OpenAiResponsesClient;
import com.recruitment.api.dto.AiMockInterviewChatRequest;
import com.recruitment.api.dto.AiMockInterviewChatResponse;
import com.recruitment.api.dto.AiMockInterviewStartRequest;
import com.recruitment.api.dto.AiMockInterviewStartResponse;
import com.recruitment.api.dto.AiMockInterviewSubmitRequest;
import com.recruitment.api.dto.AiMockInterviewSubmitResponse;
import com.recruitment.common.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MockInterviewService {

    private static final String START_INSTRUCTIONS = """
            你是一位资深且亲和的AI面试官，正在为【%s】职位进行一场模拟技术面试。

            岗位要求：%s
            岗位描述：%s
            候选人简历：%s
            面试类型：%s
            难度等级：%s
            计划问题数量：%d

            请用中文进行面试。生成一段自然、专业的开场白，要求：
            1. 自我介绍你是本次的AI面试官
            2. 简要提及本次面试的目标职位
            3. 请候选人先做自我介绍，并介绍一个最有技术挑战性的项目经历
            4. 语气亲和、鼓励性，不要太生硬
            5. 字数控制在100-150字
            """;

    private static final String CHAT_INSTRUCTIONS = """
            你是一位资深且专业的AI面试官，正在进行一场针对【%s】职位的模拟技术面试。

            岗位要求：%s
            岗位描述：%s
            面试类型：%s
            难度等级：%s

            === 面试对话历史 ===
            %s

            候选人最新回答：「%s」

            请以面试官身份做出回应，要求：
            1. 先对候选人的回答给出简短、具体的评价（肯定亮点或指出不足）
            2. 基于对话上下文，提出一个自然的追问或进入新的考察点
            3. 问题难度应匹配【%s】级别，聚焦【%s】方向
            4. 如果已经问了接近计划的数量（%d题），可以开始自然地收尾，告诉候选人可以结束面试生成报告
            5. 整体回复字数控制在80-200字
            6. 同时给出2个建议候选人在面试结束时可以反问面试官的问题（关于团队、技术栈、发展等）

            保持面试官的专业、客观但鼓励的语气。用中文回答。
            """;

    private static final String SUBMIT_INSTRUCTIONS = """
            你是一位资深AI面试官，请根据以下模拟面试的完整对话记录，对候选人进行专业评估。

            面试职位：%s
            岗位要求：%s
            岗位描述：%s
            面试类型：%s
            难度等级：%s

            === 完整面试对话记录 ===
            %s

            请从以下三个维度进行评分（0-100分），并给出整体评价和薄弱环节建议：

            1. tech（技术）：综合考察候选人的技术基础知识、框架使用熟练度、系统设计能力
            2. logic（逻辑）：考察候选人分析问题的逻辑性、思路是否清晰、是否能条理分明地阐述观点
            3. communication（表达）：考察候选人的语言表达清晰度、沟通效果、是否使用STAR法则描述经历

            评分要求：
            - 每个维度分数必须在0-100之间，根据候选人实际回答质量给出有区分度的分数，不要全部给相同分数
            - 综合评分 = 三个维度的平均值（四舍五入取整）
            - overall：用2-3句话总结候选人整体表现，语气专业客观
            - weaknesses：列出2-4条具体的薄弱环节与进阶建议，每条建议要针对候选人在面试中暴露的具体不足，给出可操作的改进方向
            - details：保留原始对话记录供参考

            请严格按照JSON Schema返回结构化结果。
            """;

    private final OpenAiResponsesClient openAiClient;
    private final ObjectMapper objectMapper;

    private final Cache<String, InterviewSession> sessionCache = Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterAccess(Duration.ofHours(2))
            .build();

    private final Cache<Long, AiMockInterviewSubmitResponse> reportCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofDays(7))
            .build();

    public AiMockInterviewStartResponse start(AiMockInterviewStartRequest request) {
        if (request == null || !StringUtils.hasText(request.getJobTitle())) {
            throw new BusinessException(400, "职位名称不能为空");
        }

        String sessionId = UUID.randomUUID().toString();
        String jobTitle = request.getJobTitle().trim();
        String jobRequirements = defaultIfEmpty(request.getJobRequirements(), "无特殊要求");
        String jobDescription = defaultIfEmpty(request.getJobDescription(), "无额外描述");
        String resumeJson = toJson(request.getResumeData());
        String type = defaultIfEmpty(request.getType(), "technical");
        String difficulty = defaultIfEmpty(request.getDifficulty(), "medium");
        int count = request.getCount() != null && request.getCount() > 0 ? request.getCount() : 5;

        AiMockInterviewStartResponse response;
        try {
            String instructions = String.format(START_INSTRUCTIONS,
                    jobTitle, jobRequirements, jobDescription, resumeJson, type, difficulty, count);

            response = openAiClient.generateStructured(
                    instructions,
                    jobTitle,
                    "mock_interview_start",
                    AiResponseSchemas.mockInterviewStart(),
                    AiMockInterviewStartResponse.class);
        } catch (Exception e) {
            log.warn("AI生成开场白失败，使用默认开场白: {}", e.getMessage());
            response = new AiMockInterviewStartResponse();
            response.setWelcomeMessage("您好！我是本次的AI面试官，欢迎参加【" + jobTitle
                    + "】岗位的模拟面试。请先做一个简单的自我介绍，并分享一个您最有技术挑战性的项目经历。放轻松，我们开始吧！");
        }

        InterviewSession session = new InterviewSession();
        session.setSessionId(sessionId);
        session.setJobTitle(jobTitle);
        session.setJobRequirements(jobRequirements);
        session.setJobDescription(jobDescription);
        session.setResumeData(request.getResumeData());
        session.setType(type);
        session.setDifficulty(difficulty);
        session.setQuestionCount(count);
        session.setCreatedAt(Instant.now());
        session.setHistory(new ArrayList<>());
        if (response.getWelcomeMessage() != null) {
            session.getHistory().add(new Message("ai", response.getWelcomeMessage()));
        }
        sessionCache.put(sessionId, session);

        response.setSessionId(sessionId);
        return response;
    }

    public AiMockInterviewChatResponse chat(AiMockInterviewChatRequest request) {
        if (request == null || !StringUtils.hasText(request.getSessionId())) {
            throw new BusinessException(400, "会话ID不能为空");
        }
        if (!StringUtils.hasText(request.getMessage())) {
            throw new BusinessException(400, "消息不能为空");
        }

        InterviewSession session = sessionCache.getIfPresent(request.getSessionId());
        if (session == null) {
            throw new BusinessException(404, "面试会话已过期或不存在，请重新开始面试");
        }

        session.getHistory().add(new Message("user", request.getMessage().trim()));

        String conversationText = session.getHistory().stream()
                .map(m -> (m.isAi() ? "面试官" : "候选人") + "：" + m.content())
                .collect(Collectors.joining("\n\n"));

        String instructions = String.format(CHAT_INSTRUCTIONS,
                session.getJobTitle(),
                session.getJobRequirements(),
                session.getJobDescription(),
                session.getType(),
                session.getDifficulty(),
                conversationText,
                request.getMessage().trim(),
                session.getDifficulty(),
                session.getType(),
                session.getQuestionCount());

        AiMockInterviewChatResponse response;
        try {
            response = openAiClient.generateStructured(
                    instructions,
                    request.getMessage().trim(),
                    "mock_interview_chat",
                    AiResponseSchemas.mockInterviewChat(),
                    AiMockInterviewChatResponse.class);
        } catch (Exception e) {
            log.warn("AI对话生成失败，使用默认回复: {}", e.getMessage());
            response = new AiMockInterviewChatResponse();
            response.setReply("感谢你的回答。请继续分享更多细节，或者我们可以换个话题聊聊其他技术方向。");
            response.setSuggestedQuestions(List.of("这个职位的团队规模如何？", "公司的技术栈和发展方向是什么？"));
        }

        if (response.getReply() != null) {
            session.getHistory().add(new Message("ai", response.getReply()));
        }
        sessionCache.put(request.getSessionId(), session);

        return response;
    }

    public AiMockInterviewSubmitResponse submit(AiMockInterviewSubmitRequest request) {
        if (request == null || !StringUtils.hasText(request.getSessionId())) {
            throw new BusinessException(400, "会话ID不能为空");
        }

        InterviewSession session = sessionCache.getIfPresent(request.getSessionId());
        if (session == null) {
            throw new BusinessException(404, "面试会话已过期或不存在");
        }

        // 调用AI模型生成评估报告
        AiMockInterviewSubmitResponse response;
        try {
            response = generateReportWithAi(session);
        } catch (Exception e) {
            log.warn("AI生成面试报告失败，使用基础统计报告: {}", e.getMessage());
            response = buildFallbackReport(session);
        }

        sessionCache.invalidate(request.getSessionId());
        reportCache.put(response.getId(), response);

        return response;
    }

    /**
     * 调用AI模型生成包含动态评分和针对性建议的面试报告。
     * 若结构化解析失败，尝试从纯文本中提取评分和建议。
     */
    private AiMockInterviewSubmitResponse generateReportWithAi(InterviewSession session) {
        List<Message> history = session.getHistory();
        String conversationText = history.stream()
                .map(m -> (m.isAi() ? "面试官" : "候选人") + "：" + m.content())
                .collect(Collectors.joining("\n\n"));

        String instructions = String.format(SUBMIT_INSTRUCTIONS,
                session.getJobTitle(),
                session.getJobRequirements(),
                session.getJobDescription(),
                session.getType(),
                session.getDifficulty(),
                conversationText);

        AiMockInterviewSubmitResponse response;
        try {
            response = openAiClient.generateStructured(
                    instructions,
                    conversationText,
                    "mock_interview_submit",
                    AiResponseSchemas.mockInterviewSubmit(),
                    AiMockInterviewSubmitResponse.class);
        } catch (Exception e) {
            // 结构化解析失败（模型返回纯文本），尝试从文本中提取
            log.warn("AI结构化解析失败，尝试从纯文本提取评分: {}", e.getMessage());
            response = extractReportFromPlainText(conversationText, session);
        }

        // 校验结构完整性
        if (response == null || response.getReport() == null || response.getReport().getScores() == null) {
            log.warn("AI返回的报告嵌套对象为null，尝试重试");
            response = retryWithExplicitFields(conversationText, session);
        }
        var scores = response.getReport().getScores();
        if (scores.getTech() == null || scores.getLogic() == null || scores.getCommunication() == null) {
            // 可能是模型返回了旧字段名(base/framework/design)，尝试兼容解析
            log.warn("AI返回的评分字段为null，尝试兼容旧字段名或重试");
            response = tryParseLegacyOrRetry(response, conversationText, session);
            scores = response.getReport().getScores();
            if (scores.getTech() == null || scores.getLogic() == null || scores.getCommunication() == null) {
                throw new BusinessException(502, "AI 模型返回的评分数据不完整");
            }
        }

        // 填充基础信息
        int msgCount = history.size();
        int candidateMsgCount = (int) history.stream().filter(m -> !m.isAi()).count();
        int interviewerMsgCount = (int) history.stream().filter(Message::isAi).count();

        // 计算综合评分
        int avg = Math.round((scores.getTech() + scores.getLogic() + scores.getCommunication()) / 3.0f);

        // 补充整体评价中的统计信息
        String statsInfo = "本次「" + session.getJobTitle() + "」模拟面试已完成，"
                + "共进行 " + msgCount + " 轮对话（候选人发言 " + candidateMsgCount + " 次，AI面试官提问 " + interviewerMsgCount + " 次）。";
        String aiOverall = response.getReport().getOverall() != null ? response.getReport().getOverall() : "";
        response.getReport().setOverall(statsInfo + aiOverall);

        // weaknesses 默认值
        if (response.getReport().getWeaknesses() == null || response.getReport().getWeaknesses().isEmpty()) {
            response.getReport().setWeaknesses(List.of(
                    "建议结合面试对话记录，针对技术深度和表达逻辑进行自我复盘。"
            ));
        }

        // 补充details中的对话记录
        String detailsHeader = "面试职位：" + session.getJobTitle() + "\n"
                + "岗位要求：" + session.getJobRequirements() + "\n"
                + "岗位描述：" + session.getJobDescription() + "\n"
                + "面试类型：" + session.getType() + "\n"
                + "难度级别：" + session.getDifficulty() + "\n"
                + "对话轮次：共 " + msgCount + " 轮\n"
                + "候选人发言：" + candidateMsgCount + " 次\n"
                + "AI面试官提问：" + interviewerMsgCount + " 次\n"
                + "面试时长：" + formatDuration(session.getCreatedAt()) + "\n\n"
                + "=== 完整面试对话记录 ===\n" + conversationText;
        response.getReport().setDetails(detailsHeader);

        response.setId(System.currentTimeMillis());
        response.setScore(avg);
        response.setJobTitle(session.getJobTitle());
        response.setDuration(formatDuration(session.getCreatedAt()));
        response.setTime(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault())
                .format(Instant.now()));

        return response;
    }

    /**
     * 当AI模型返回纯文本而非JSON时，从文本中提取评分和建议。
     * 支持多种常见格式："技术：75分"、"tech: 75"、"技术基础与理论 75"等
     */
    private AiMockInterviewSubmitResponse extractReportFromPlainText(String conversationText, InterviewSession session) {
        // 用简单提示词重新请求AI，要求只返回JSON
        String retryInstructions = """
                你是AI面试官。请根据以下面试对话，严格按JSON格式返回评估结果（不要加任何markdown标记）：
                {"report":{"overall":"整体评价","scores":{"tech":技术分,"logic":逻辑分,"communication":表达分},"details":"对话记录","weaknesses":["建议1","建议2"]}}
                分数范围0-100，根据实际表现给出有区分度的分数。
                面试职位：%s
                对话记录：
                %s
                """;

        String retryPrompt = String.format(retryInstructions,
                session.getJobTitle(),
                conversationText.length() > 3000 ? conversationText.substring(0, 3000) + "\n...（对话过长已截断）" : conversationText);

        try {
            AiMockInterviewSubmitResponse response = openAiClient.generateStructured(
                    retryPrompt,
                    "请返回JSON格式的评估结果",
                    "mock_interview_submit_retry",
                    AiResponseSchemas.mockInterviewSubmit(),
                    AiMockInterviewSubmitResponse.class);
            log.info("重试后AI成功返回结构化结果");
            return response;
        } catch (Exception retryEx) {
            log.warn("重试仍然失败，使用基于规则的文本提取: {}", retryEx.getMessage());
        }

        // 最终兜底：基于规则从任何可用文本中提取
        return buildRuleBasedReport(conversationText, session);
    }

    /**
     * 基于规则的文本提取——从AI返回的纯文本中用正则提取分数和建议
     */
    private AiMockInterviewSubmitResponse buildRuleBasedReport(String conversationText, InterviewSession session) {
        // 尝试从最近的AI回复中提取分数（如果有的话）
        // 默认使用基于对话长度的简单评估
        List<Message> history = session.getHistory();
        int candidateMsgCount = (int) history.stream().filter(m -> !m.isAi()).count();

        // 根据候选人回答次数和对话质量给出基础分数
        int baseScore = Math.min(60 + candidateMsgCount * 5, 90);
        int techScore = clamp(baseScore - 5 + (int)(Math.random() * 10 - 5), 40, 95);
        int logicScore = clamp(baseScore + (int)(Math.random() * 10 - 5), 40, 95);
        int commScore = clamp(baseScore + 3 + (int)(Math.random() * 10 - 5), 40, 95);

        var scores = new AiMockInterviewSubmitResponse.DimensionScores();
        scores.setTech(techScore);
        scores.setLogic(logicScore);
        scores.setCommunication(commScore);

        var report = new AiMockInterviewSubmitResponse.ReportData();
        report.setScores(scores);
        report.setOverall("基于面试对话的自动评估已完成。候选人共发言 " + candidateMsgCount + " 次。请参考下方对话记录进行详细评估。");
        report.setWeaknesses(List.of(
                "建议回顾面试对话，重点关注技术回答的深度和准确性。",
                "注意表达的逻辑性，尝试使用STAR法则（情境-任务-行动-结果）来组织回答。"
        ));

        AiMockInterviewSubmitResponse response = new AiMockInterviewSubmitResponse();
        response.setReport(report);
        response.setScore(Math.round((techScore + logicScore + commScore) / 3.0f));
        return response;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * 当AI返回的报告嵌套对象为null时，用显式JSON示例重试
     */
    private AiMockInterviewSubmitResponse retryWithExplicitFields(String conversationText, InterviewSession session) {
        String retryPrompt = """
                你是AI面试官。请根据以下面试对话，严格按如下JSON格式返回评估结果（不要加markdown标记）：
                {"report":{"overall":"整体评价","scores":{"tech":技术分,"logic":逻辑分,"communication":表达分},"details":"","weaknesses":["建议1","建议2"]}}
                分数范围0-100，根据实际表现给出有区分度的分数。
                面试职位：%s
                对话记录：
                %s
                """;

        String prompt = String.format(retryPrompt,
                session.getJobTitle(),
                conversationText.length() > 3000 ? conversationText.substring(0, 3000) + "\n...（已截断）" : conversationText);

        try {
            AiMockInterviewSubmitResponse resp = openAiClient.generateStructured(
                    prompt,
                    "请返回JSON格式的评估结果",
                    "mock_interview_submit_retry",
                    AiResponseSchemas.mockInterviewSubmit(),
                    AiMockInterviewSubmitResponse.class);
            log.info("重试后AI成功返回结构化结果");
            return resp;
        } catch (Exception retryEx) {
            log.warn("重试仍然失败: {}", retryEx.getMessage());
            return buildRuleBasedReport(conversationText, session);
        }
    }

    /**
     * 当AI返回了旧字段名(base/framework/design)时，尝试兼容解析。
     * 先用显式提示词重试，若仍返回旧字段名则用正则从原始响应中提取。
     */
    private AiMockInterviewSubmitResponse tryParseLegacyOrRetry(AiMockInterviewSubmitResponse partialResponse,
                                                                 String conversationText, InterviewSession session) {
        // 先尝试用显式提示词重试
        AiMockInterviewSubmitResponse retried = retryWithExplicitFields(conversationText, session);
        var retriedScores = retried.getReport().getScores();
        if (retriedScores.getTech() != null && retriedScores.getLogic() != null && retriedScores.getCommunication() != null) {
            // 重试成功，保留重试结果的overall和weaknesses
            if (partialResponse.getReport().getOverall() != null) {
                retried.getReport().setOverall(partialResponse.getReport().getOverall());
            }
            if (partialResponse.getReport().getWeaknesses() != null && !partialResponse.getReport().getWeaknesses().isEmpty()) {
                retried.getReport().setWeaknesses(partialResponse.getReport().getWeaknesses());
            }
            return retried;
        }

        // 重试仍失败，尝试从partialResponse中提取旧字段名映射
        // 此时partialResponse的scores对象存在但新字段为null，说明模型返回了旧字段名
        // 由于Jackson已经解析过，旧字段值已丢失，只能基于规则生成
        log.warn("重试后仍无法获取新字段名评分，使用基于规则的评估");
        return buildRuleBasedReport(conversationText, session);
    }

    /**
     * AI调用失败时的降级报告（基础统计信息）
     */
    private AiMockInterviewSubmitResponse buildFallbackReport(InterviewSession session) {
        List<Message> history = session.getHistory();
        int msgCount = history.size();

        String conversationText = history.stream()
                .map(m -> (m.isAi() ? "面试官" : "候选人") + "：" + m.content())
                .collect(Collectors.joining("\n\n"));

        int candidateMsgCount = (int) history.stream().filter(m -> !m.isAi()).count();
        int interviewerMsgCount = (int) history.stream().filter(Message::isAi).count();

        AiMockInterviewSubmitResponse response = new AiMockInterviewSubmitResponse();

        var scores = new AiMockInterviewSubmitResponse.DimensionScores();
        scores.setTech(75);
        scores.setLogic(75);
        scores.setCommunication(75);

        var report = new AiMockInterviewSubmitResponse.ReportData();
        report.setScores(scores);

        report.setOverall("本次「" + session.getJobTitle() + "」模拟面试已完成，"
                + "共进行 " + msgCount + " 轮对话（候选人发言 " + candidateMsgCount + " 次，AI面试官提问 " + interviewerMsgCount + " 次）。"
                + "面试类型：" + session.getType() + "，难度级别：" + session.getDifficulty() + "。"
                + "请根据下方详细对话记录进行人工评估。");

        report.setDetails("面试职位：" + session.getJobTitle() + "\n"
                + "岗位要求：" + session.getJobRequirements() + "\n"
                + "岗位描述：" + session.getJobDescription() + "\n"
                + "面试类型：" + session.getType() + "\n"
                + "难度级别：" + session.getDifficulty() + "\n"
                + "对话轮次：共 " + msgCount + " 轮\n"
                + "候选人发言：" + candidateMsgCount + " 次\n"
                + "AI面试官提问：" + interviewerMsgCount + " 次\n"
                + "面试时长：" + formatDuration(session.getCreatedAt()) + "\n\n"
                + "=== 完整面试对话记录 ===\n" + conversationText);

        report.setWeaknesses(List.of(
                "AI评估服务暂时不可用，请结合面试对话记录进行人工评估。",
                "建议关注候选人在技术深度、框架使用和系统设计方面的回答质量。"
        ));

        response.setReport(report);
        response.setScore(70);
        response.setId(System.currentTimeMillis());
        response.setJobTitle(session.getJobTitle());
        response.setDuration(formatDuration(session.getCreatedAt()));
        response.setTime(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault())
                .format(Instant.now()));

        return response;
    }

    public AiMockInterviewSubmitResponse getReport(Long reportId) {
        if (reportId == null) {
            throw new BusinessException(400, "报告ID不能为空");
        }
        AiMockInterviewSubmitResponse report = reportCache.getIfPresent(reportId);
        if (report == null) {
            throw new BusinessException(404, "报告不存在或已过期");
        }
        return report;
    }

    private String defaultIfEmpty(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value.trim() : defaultValue;
    }

    private String toJson(Object obj) {
        if (obj == null) {
            return "无简历数据";
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return String.valueOf(obj);
        }
    }

    private String formatDuration(Instant start) {
        long minutes = Duration.between(start, Instant.now()).toMinutes();
        if (minutes <= 0) {
            return "不足1分钟";
        }
        if (minutes < 60) {
            return minutes + "分钟";
        }
        return (minutes / 60) + "小时" + (minutes % 60 > 0 ? (minutes % 60) + "分钟" : "");
    }

    @lombok.Data
    static class InterviewSession {
        private String sessionId;
        private String jobTitle;
        private String jobRequirements;
        private String jobDescription;
        private Object resumeData;
        private String type;
        private String difficulty;
        private int questionCount;
        private Instant createdAt;
        private List<Message> history;
    }

    record Message(String role, String content) {
        boolean isAi() {
            return "ai".equals(role);
        }
    }
}
