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

        // 直接基于已有对话数据生成报告，不依赖AI模型（qwen长对话评估不稳定）
        AiMockInterviewSubmitResponse response = buildReportFromSession(session);

        sessionCache.invalidate(request.getSessionId());
        reportCache.put(response.getId(), response);

        return response;
    }

    private AiMockInterviewSubmitResponse buildReportFromSession(InterviewSession session) {
        List<Message> history = session.getHistory();
        int msgCount = history.size();

        String conversationText = history.stream()
                .map(m -> (m.isAi() ? "面试官" : "候选人") + "：" + m.content())
                .collect(Collectors.joining("\n\n"));

        // 统计基础信息
        int candidateMsgCount = (int) history.stream().filter(m -> !m.isAi()).count();
        int interviewerMsgCount = (int) history.stream().filter(Message::isAi).count();

        AiMockInterviewSubmitResponse response = new AiMockInterviewSubmitResponse();

        var scores = new AiMockInterviewSubmitResponse.DimensionScores();
        scores.setBase(75);
        scores.setFramework(75);
        scores.setDesign(75);
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

        response.setReport(report);
        response.setScore(70);  // 默认评分
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
