package com.recruitment.interview.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruitment.api.client.AiServiceClient;
import com.recruitment.api.dto.AiQuestionRequest;
import com.recruitment.api.dto.AiQuestionResponse;
import com.recruitment.common.core.model.Result;
import com.recruitment.interview.entity.InterviewQuestion;
import com.recruitment.interview.mapper.InterviewQuestionMapper;
import com.recruitment.job.entity.Job;
import com.recruitment.job.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 面试题管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/interview/question")
@RequiredArgsConstructor
public class InterviewQuestionController {

    private final AiServiceClient aiServiceClient;
    private final InterviewQuestionMapper interviewQuestionMapper;
    private final JobService jobService;

    /**
     * AI生成面试题
     */
    @PostMapping("/generate")
    public Result<AiQuestionResponse> generate(@RequestBody Map<String, Object> body) {
        // 构建AI请求
        AiQuestionRequest request = new AiQuestionRequest();

        // 优先使用传入的 jobTitle / jobDescription，否则从 jobId 加载
        String jobTitle = body.get("jobTitle") != null ? body.get("jobTitle").toString() : null;
        String jobDescription = body.get("jobDescription") != null ? body.get("jobDescription").toString() : null;
        Object jobIdObj = body.get("jobId");

        if (!StringUtils.hasText(jobTitle) && jobIdObj != null) {
            Long jobId = Long.valueOf(jobIdObj.toString());
            Job job = jobService.getById(jobId);
            if (job != null) {
                jobTitle = job.getJobName();
                jobDescription = job.getDescription();
            }
        }

        if (!StringUtils.hasText(jobTitle) || !StringUtils.hasText(jobDescription)) {
            return Result.error(400, "职位名称和职位描述不能为空，请选择目标职位");
        }

        request.setJobTitle(jobTitle);
        request.setJobDescription(jobDescription);

        if (body.get("resumeContent") != null) {
            request.setResumeContent(body.get("resumeContent").toString());
        }
        if (body.get("questionType") != null) {
            request.setQuestionType(body.get("questionType").toString());
        }
        if (body.get("difficulty") != null) {
            request.setDifficulty(body.get("difficulty").toString());
        }

        Integer count = 5;
        if (body.get("count") != null) {
            count = Integer.valueOf(body.get("count").toString());
        }
        request.setCount(count);

        log.info("开始生成面试题: jobTitle={}, count={}, difficulty={}",
                jobTitle, count, request.getDifficulty());

        try {
            Result<AiQuestionResponse> aiResult = aiServiceClient.generateQuestions(request);
            if (aiResult.getData() != null) {
                log.info("AI生成面试题成功，共{}道", aiResult.getData().getQuestions().size());
            }
            return aiResult;
        } catch (Exception e) {
            log.error("AI生成面试题失败: {}", e.getMessage(), e);
            return Result.error(503, "AI服务暂不可用，请稍后重试");
        }
    }

    /**
     * 保存正式面试题（面试官确认并保存）
     */
    @PostMapping
    public Result<List<InterviewQuestion>> save(@RequestBody Map<String, Object> body) {
        Long interviewId = body.get("interviewId") != null
                ? Long.valueOf(body.get("interviewId").toString()) : null;
        if (interviewId == null) {
            return Result.error(400, "面试ID不能为空");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> questions = (List<Map<String, Object>>) body.get("questions");
        if (questions == null || questions.isEmpty()) {
            return Result.error(400, "面试题列表不能为空");
        }

        // 先删除该面试的旧题目
        interviewQuestionMapper.delete(
                new LambdaQueryWrapper<InterviewQuestion>()
                        .eq(InterviewQuestion::getInterviewId, interviewId));

        // 批量保存新题目
        List<InterviewQuestion> saved = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            Map<String, Object> q = questions.get(i);
            InterviewQuestion entity = new InterviewQuestion();
            entity.setInterviewId(interviewId);
            entity.setTitle(q.get("title") != null ? q.get("title").toString() : "");
            entity.setQuestionType(q.get("type") != null ? q.get("type").toString() : null);
            entity.setDifficulty(q.get("difficulty") != null ? q.get("difficulty").toString() : null);
            entity.setReferenceAnswer(q.get("referenceAnswer") != null ? q.get("referenceAnswer").toString() : null);
            entity.setScoringCriteria(q.get("scoringCriteria") != null ? q.get("scoringCriteria").toString() : null);
            entity.setSortOrder(i + 1);
            entity.setIsAiGenerated(true);
            interviewQuestionMapper.insert(entity);
            saved.add(entity);
        }

        log.info("面试题保存成功: interviewId={}, count={}", interviewId, saved.size());
        return Result.success(saved);
    }

    /**
     * 根据面试ID查询面试题
     */
    @GetMapping("/{interviewId}")
    public Result<List<InterviewQuestion>> listByInterview(@PathVariable Long interviewId) {
        List<InterviewQuestion> questions = interviewQuestionMapper.selectList(
                new LambdaQueryWrapper<InterviewQuestion>()
                        .eq(InterviewQuestion::getInterviewId, interviewId)
                        .orderByAsc(InterviewQuestion::getSortOrder));
        return Result.success(questions);
    }

    /**
     * 候选人查看自己面试的题目（不含参考答案）
     */
    @GetMapping("/candidate/{interviewId}")
    public Result<List<Map<String, Object>>> listForCandidate(@PathVariable Long interviewId) {
        List<InterviewQuestion> questions = interviewQuestionMapper.selectList(
                new LambdaQueryWrapper<InterviewQuestion>()
                        .eq(InterviewQuestion::getInterviewId, interviewId)
                        .orderByAsc(InterviewQuestion::getSortOrder));
        List<Map<String, Object>> result = new ArrayList<>();
        for (InterviewQuestion q : questions) {
            Map<String, Object> item = new java.util.LinkedHashMap<>();
            item.put("id", q.getId());
            item.put("title", q.getTitle());
            item.put("questionType", q.getQuestionType());
            item.put("difficulty", q.getDifficulty());
            item.put("candidateAnswer", q.getCandidateAnswer());
            item.put("answerTime", q.getAnswerTime());
            result.add(item);
        }
        return Result.success(result);
    }

    /**
     * 候选人提交面试题答案
     */
    @PutMapping("/answer")
    public Result<?> submitAnswer(@RequestBody Map<String, Object> body) {
        Long questionId = body.get("questionId") != null
                ? Long.valueOf(body.get("questionId").toString()) : null;
        String answer = body.get("answer") != null ? body.get("answer").toString() : null;

        if (questionId == null) {
            return Result.error(400, "题目ID不能为空");
        }

        InterviewQuestion question = interviewQuestionMapper.selectById(questionId);
        if (question == null) {
            return Result.error(404, "题目不存在");
        }

        question.setCandidateAnswer(answer);
        question.setAnswerTime(java.time.LocalDateTime.now());
        interviewQuestionMapper.updateById(question);

        log.info("候选人提交答案: questionId={}", questionId);
        return Result.success("答案已保存");
    }
}
