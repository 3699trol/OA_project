package com.recruitment.interview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruitment.application.entity.JobApplication;
import com.recruitment.application.mapper.JobApplicationMapper;
import com.recruitment.mail.MailService;
import com.recruitment.common.core.model.PageResult;
import com.recruitment.common.redis.util.DailyStatsCounter;
import com.recruitment.interview.dto.InterviewCreateRequest;
import com.recruitment.interview.dto.InterviewEvaluationRequest;
import com.recruitment.interview.entity.Interview;
import com.recruitment.interview.entity.InterviewEvaluation;
import com.recruitment.interview.entity.InterviewQuestion;
import com.recruitment.interview.mapper.InterviewEvaluationMapper;
import com.recruitment.interview.mapper.InterviewMapper;
import com.recruitment.interview.mapper.InterviewQuestionMapper;
import com.recruitment.interview.service.InterviewService;
import com.recruitment.job.entity.Job;
import com.recruitment.job.mapper.JobMapper;
import com.recruitment.resume.entity.Resume;
import com.recruitment.resume.mapper.ResumeMapper;
import com.recruitment.system.entity.SysUser;
import com.recruitment.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 面试服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final InterviewMapper interviewMapper;
    private final InterviewEvaluationMapper evaluationMapper;
    private final InterviewQuestionMapper questionMapper;
    private final JobApplicationMapper applicationMapper;
    private final JobMapper jobMapper;
    private final SysUserMapper userMapper;
    private final ResumeMapper resumeMapper;
    private final MailService mailService;
    private final ObjectProvider<DailyStatsCounter> dailyStatsCounterProvider;

    /** 今日面试数计数器业务域 */
    private static final String DOMAIN_INTERVIEW = "interview";

    @Override
    public Map<String, Object> createInterview(InterviewCreateRequest request, Long operatorId) {
        Long applicationId = request.getApplicationId();
        Long interviewerId = request.getInterviewerId();

        // 校验投递记录存在
        JobApplication application = applicationMapper.selectById(applicationId);
        if (application == null || application.getDeleted() == 1) {
            throw new RuntimeException("投递记录不存在");
        }
        if (!request.getCandidateId().equals(application.getUserId())) {
            throw new RuntimeException("候选人与投递记录不匹配");
        }

        // 校验投递状态：只有"面试中(1)"或"已录用(2)"的投递才能安排面试
        Integer appStatus = application.getStatus();
        if (appStatus == null || appStatus == 0) {
            throw new RuntimeException("该候选人尚未通过初筛，无法安排面试");
        }
        if (appStatus == 3) {
            throw new RuntimeException("该候选人已被标记为不合适，无法安排面试");
        }
        if (appStatus == 4) {
            throw new RuntimeException("该候选人的投递已撤回，无法安排面试");
        }

        // 校验是否已安排面试（防止重复安排）
        LambdaQueryWrapper<Interview> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(Interview::getApplicationId, applicationId)
                .ne(Interview::getStatus, 2); // 排除已取消的面试
        Long existCount = interviewMapper.selectCount(existWrapper);
        if (existCount != null && existCount > 0) {
            throw new RuntimeException("该候选人已安排面试，请勿重复安排。如需调整请在面试详情中操作");
        }

        // 解析面试时间
        LocalDateTime interviewTime;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            interviewTime = LocalDateTime.parse(request.getInterviewTime(), formatter);
        } catch (Exception e) {
            throw new RuntimeException("面试时间格式错误，请使用 yyyy-MM-dd HH:mm 格式");
        }

        // 构建面试类型（合并 type + mode）
        String type = request.getType();
        String mode = request.getMode();
        String interviewType = type + "-" + mode;

        // 地址/备注
        String address = request.getAddress();

        // 创建面试记录
        Interview interview = new Interview();
        interview.setApplicationId(applicationId);
        interview.setInterviewerId(interviewerId);
        interview.setInterviewTime(interviewTime);
        interview.setInterviewType(interviewType);
        interview.setAddress(address);
        interview.setStatus(0); // 待面试
        interviewMapper.insert(interview);

        // Redis INCR 实时统计今日面试数
        DailyStatsCounter counter = dailyStatsCounterProvider.getIfAvailable();
        if (counter != null) {
            counter.incrementToday(DOMAIN_INTERVIEW);
        }

        // 发送面试邀请邮件（异步）
        sendInterviewInvitationEmail(application, interviewTime, interviewerId, address);

        // 同步更新投递状态为面试中(1)
        if (application.getStatus() == 0) {
            application.setStatus(1);
            application.setUpdateTime(LocalDateTime.now());
            applicationMapper.updateById(application);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", interview.getId());
        return result;
    }

    @Override
    public PageResult<Map<String, Object>> listInterviews(long pageNum, long pageSize, Integer status) {
        LambdaQueryWrapper<Interview> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Interview::getStatus, status);
        }
        wrapper.orderByDesc(Interview::getCreateTime);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Interview> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Interview> resultPage = interviewMapper.selectPage(page, wrapper);

        List<Map<String, Object>> records = resultPage.getRecords().stream().map(interview -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", interview.getId());
            map.put("interviewTime", interview.getInterviewTime());
            map.put("interviewType", interview.getInterviewType());
            map.put("address", interview.getAddress());
            map.put("status", interview.getStatus());

            // 关联投递 → 候选人 + 职位
            JobApplication application = applicationMapper.selectById(interview.getApplicationId());
            if (application != null) {
                map.put("applicationId", application.getId());
                SysUser candidate = userMapper.selectById(application.getUserId());
                if (candidate != null) {
                    map.put("candidateName", candidate.getRealName() != null ? candidate.getRealName() : candidate.getUsername());
                }
                Job job = jobMapper.selectById(application.getJobId());
                if (job != null) {
                    map.put("jobName", job.getJobName());
                }
            }

            // 关联面试官
            SysUser interviewer = userMapper.selectById(interview.getInterviewerId());
            if (interviewer != null) {
                map.put("interviewerName", interviewer.getRealName() != null ? interviewer.getRealName() : interviewer.getUsername());
            }

            // 状态文本
            String[] statusLabels = {"待面试", "已完成", "已取消"};
            int s = interview.getStatus() != null ? interview.getStatus() : 0;
            map.put("statusLabel", s >= 0 && s < statusLabels.length ? statusLabels[s] : "未知");

            return map;
        }).collect(Collectors.toList());

        return new PageResult<>(records, resultPage.getTotal(), pageNum, pageSize);
    }

    @Override
    public Map<String, Object> getOptions() {
        Map<String, Object> options = new HashMap<>();

        // 候选人列表：有投递记录的用户
        LambdaQueryWrapper<JobApplication> appWrapper = new LambdaQueryWrapper<>();
        appWrapper.eq(JobApplication::getDeleted, 0)
                  .select(JobApplication::getUserId);
        List<JobApplication> applications = applicationMapper.selectList(appWrapper);
        List<Long> candidateIds = applications.stream()
                .map(JobApplication::getUserId)
                .distinct()
                .collect(Collectors.toList());

        List<Map<String, Object>> candidates = candidateIds.stream().map(uid -> {
            SysUser user = userMapper.selectById(uid);
            if (user == null) return null;
            Map<String, Object> m = new HashMap<>();
            m.put("id", user.getId());
            m.put("name", user.getRealName() != null ? user.getRealName() : user.getUsername());
            return m;
        }).filter(m -> m != null).collect(Collectors.toList());
        options.put("candidates", candidates);

        // 面试官列表：userType = 3（面试官）或 2（HR）
        LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.in(SysUser::getUserType, 2, 3)
                   .eq(SysUser::getDeleted, 0)
                   .eq(SysUser::getStatus, 1);
        List<SysUser> interviewers = userMapper.selectList(userWrapper);
        List<Map<String, Object>> interviewerList = interviewers.stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("name", u.getRealName() != null ? u.getRealName() : u.getUsername());
            m.put("userType", u.getUserType());
            return m;
        }).collect(Collectors.toList());
        options.put("interviewers", interviewerList);

        return options;
    }

    @Override
    public List<Map<String, Object>> getCandidateApplications(Long userId) {
        LambdaQueryWrapper<JobApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobApplication::getUserId, userId)
               .eq(JobApplication::getDeleted, 0)
               .orderByDesc(JobApplication::getCreateTime);
        List<JobApplication> applications = applicationMapper.selectList(wrapper);

        return applications.stream().map(app -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", app.getId());
            map.put("status", app.getStatus());
            map.put("applyTime", app.getApplyTime());

            Job job = jobMapper.selectById(app.getJobId());
            if (job != null) {
                map.put("jobName", job.getJobName());
                map.put("department", job.getDepartment());
            }

            String[] statusLabels = {"待筛选", "面试中", "已录用", "不合适", "已撤回"};
            int s = app.getStatus() != null ? app.getStatus() : 0;
            map.put("statusLabel", s >= 0 && s < statusLabels.length ? statusLabels[s] : "未知");

            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> listByCandidate(Long userId) {
        // 查找该用户的所有投递记录
        LambdaQueryWrapper<JobApplication> appWrapper = new LambdaQueryWrapper<>();
        appWrapper.eq(JobApplication::getUserId, userId)
                  .eq(JobApplication::getDeleted, 0);
        List<JobApplication> applications = applicationMapper.selectList(appWrapper);

        if (applications.isEmpty()) {
            return List.of();
        }

        List<Long> applicationIds = applications.stream()
                .map(JobApplication::getId).collect(Collectors.toList());

        // 查找这些投递关联的面试
        LambdaQueryWrapper<Interview> interviewWrapper = new LambdaQueryWrapper<>();
        interviewWrapper.in(Interview::getApplicationId, applicationIds)
                        .orderByDesc(Interview::getInterviewTime);
        List<Interview> interviews = interviewMapper.selectList(interviewWrapper);

        return interviews.stream().map(interview -> {
            Map<String, Object> map = buildInterviewMap(interview);

            // 附加面试题数量
            LambdaQueryWrapper<InterviewQuestion> qWrapper = new LambdaQueryWrapper<>();
            qWrapper.eq(InterviewQuestion::getInterviewId, interview.getId());
            long questionCount = questionMapper.selectCount(qWrapper);
            map.put("questionCount", questionCount);

            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public PageResult<Map<String, Object>> listByInterviewer(long pageNum, long pageSize, Integer status, Long interviewerId) {
        LambdaQueryWrapper<Interview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Interview::getInterviewerId, interviewerId);
        if (status != null) {
            wrapper.eq(Interview::getStatus, status);
        }
        wrapper.orderByDesc(Interview::getInterviewTime);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Interview> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Interview> resultPage = interviewMapper.selectPage(page, wrapper);

        List<Map<String, Object>> records = resultPage.getRecords().stream().map(this::buildInterviewMap).collect(Collectors.toList());
        return new PageResult<>(records, resultPage.getTotal(), pageNum, pageSize);
    }

    @Override
    public List<Map<String, Object>> listTodayByInterviewer(Long interviewerId) {
        if (interviewerId == null) {
            return List.of();
        }
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);

        LambdaQueryWrapper<Interview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Interview::getInterviewerId, interviewerId)
               .ge(Interview::getInterviewTime, dayStart)
               .lt(Interview::getInterviewTime, dayEnd)
               .orderByAsc(Interview::getInterviewTime);

        List<Interview> interviews = interviewMapper.selectList(wrapper);
        return interviews.stream().map(this::buildInterviewMap).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> listRecentEvaluationsByInterviewer(Long interviewerId, int limit) {
        if (interviewerId == null) {
            return List.of();
        }
        // 先取该面试官的所有面试ID
        LambdaQueryWrapper<Interview> interviewWrapper = new LambdaQueryWrapper<>();
        interviewWrapper.eq(Interview::getInterviewerId, interviewerId)
                        .select(Interview::getId);
        List<Interview> interviews = interviewMapper.selectList(interviewWrapper);
        if (interviews.isEmpty()) {
            return List.of();
        }
        List<Long> interviewIds = interviews.stream().map(Interview::getId).collect(Collectors.toList());

        // 查询这些面试关联的评价，按评价时间倒序
        LambdaQueryWrapper<InterviewEvaluation> evalWrapper = new LambdaQueryWrapper<>();
        evalWrapper.in(InterviewEvaluation::getInterviewId, interviewIds)
                   .orderByDesc(InterviewEvaluation::getFeedbackTime);
        if (limit > 0) {
            evalWrapper.last("LIMIT " + limit);
        }
        List<InterviewEvaluation> evaluations = evaluationMapper.selectList(evalWrapper);

        // 批量加载面试信息（含候选人/职位）
        List<Long> evalInterviewIds = evaluations.stream().map(InterviewEvaluation::getInterviewId).distinct().collect(Collectors.toList());
        Map<Long, Interview> interviewMap = new HashMap<>();
        if (!evalInterviewIds.isEmpty()) {
            interviewMapper.selectBatchIds(evalInterviewIds)
                    .forEach(i -> interviewMap.put(i.getId(), i));
        }
        // 批量加载投递 → 候选人/职位
        List<Long> applicationIds = interviewMap.values().stream()
                .map(Interview::getApplicationId).distinct().collect(Collectors.toList());
        Map<Long, JobApplication> applicationMap = new HashMap<>();
        if (!applicationIds.isEmpty()) {
            applicationMapper.selectBatchIds(applicationIds)
                    .forEach(a -> applicationMap.put(a.getId(), a));
        }
        List<Long> userIds = applicationMap.values().stream().map(JobApplication::getUserId).distinct().collect(Collectors.toList());
        List<Long> jobIds = applicationMap.values().stream().map(JobApplication::getJobId).distinct().collect(Collectors.toList());
        Map<Long, String> userNameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMapper.selectBatchIds(userIds).forEach(u ->
                    userNameMap.put(u.getId(), u.getRealName() != null ? u.getRealName() : u.getUsername()));
        }
        Map<Long, String> jobNameMap = new HashMap<>();
        if (!jobIds.isEmpty()) {
            jobMapper.selectBatchIds(jobIds).forEach(j -> jobNameMap.put(j.getId(), j.getJobName()));
        }

        String[] resultLabels = {"淘汰", "待定", "通过"};
        return evaluations.stream().map(eval -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", eval.getId());
            map.put("interviewId", eval.getInterviewId());
            map.put("technicalScore", eval.getTechnicalScore());
            map.put("communicationScore", eval.getCommunicationScore());
            map.put("logicScore", eval.getLogicScore());
            map.put("overallScore", eval.getOverallScore());
            map.put("evaluation", eval.getEvaluation());
            map.put("result", eval.getResult());
            int r = eval.getResult() != null ? eval.getResult() : 0;
            map.put("resultLabel", r >= 0 && r < resultLabels.length ? resultLabels[r] : "未知");
            map.put("feedbackTime", eval.getFeedbackTime());

            Interview interview = interviewMap.get(eval.getInterviewId());
            if (interview != null) {
                map.put("interviewTime", interview.getInterviewTime());
                JobApplication app = applicationMap.get(interview.getApplicationId());
                if (app != null) {
                    map.put("candidateName", userNameMap.getOrDefault(app.getUserId(), "未知"));
                    map.put("jobName", jobNameMap.getOrDefault(app.getJobId(), "未知"));
                }
            }
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getInterviewDetail(Long interviewId) {
        Interview interview = interviewMapper.selectById(interviewId);
        if (interview == null) {
            throw new RuntimeException("面试记录不存在");
        }
        Map<String, Object> detail = buildInterviewMap(interview);

        // 关联面试评价（防御性处理：历史可能存在重复评价记录，selectOne 会抛 TooManyResultsException）
        try {
            LambdaQueryWrapper<InterviewEvaluation> evalWrapper = new LambdaQueryWrapper<>();
            evalWrapper.eq(InterviewEvaluation::getInterviewId, interviewId)
                       .orderByDesc(InterviewEvaluation::getFeedbackTime)
                       .last("LIMIT 1");
            InterviewEvaluation evaluation = evaluationMapper.selectOne(evalWrapper);
            if (evaluation != null) {
                Map<String, Object> evalMap = new HashMap<>();
                evalMap.put("technicalScore", evaluation.getTechnicalScore());
                evalMap.put("communicationScore", evaluation.getCommunicationScore());
                evalMap.put("logicScore", evaluation.getLogicScore());
                evalMap.put("overallScore", evaluation.getOverallScore());
                evalMap.put("evaluation", evaluation.getEvaluation());
                evalMap.put("result", evaluation.getResult());
                String[] resultLabels = {"淘汰", "待定", "通过"};
                int r = evaluation.getResult() != null ? evaluation.getResult() : 0;
                evalMap.put("resultLabel", r >= 0 && r < resultLabels.length ? resultLabels[r] : "未知");
                detail.put("evaluation", evalMap);
            }
        } catch (Exception e) {
            log.warn("加载面试评价失败, interviewId={}: {}", interviewId, e.getMessage());
        }

        // 关联面试题（含候选人答案）
        LambdaQueryWrapper<InterviewQuestion> qWrapper = new LambdaQueryWrapper<>();
        qWrapper.eq(InterviewQuestion::getInterviewId, interviewId)
                .orderByAsc(InterviewQuestion::getSortOrder);
        List<InterviewQuestion> questions = questionMapper.selectList(qWrapper);
        detail.put("questions", questions);

        // 附加投递状态，供 HR 判断是否已处理
        JobApplication app = applicationMapper.selectById(interview.getApplicationId());
        if (app != null) {
            detail.put("applicationStatus", app.getStatus());
        }

        // 关联候选人简历信息，供面试官查看
        appendCandidateResume(detail, interview.getApplicationId());

        return detail;
    }

    /**
     * 将候选人简历信息附加到面试详情，供面试官查看/下载。
     * 任何异常都不影响主流程，避免简历缺失时导致整个详情加载失败。
     */
    private void appendCandidateResume(Map<String, Object> detail, Long applicationId) {
        if (applicationId == null) {
            return;
        }
        try {
            JobApplication application = applicationMapper.selectById(applicationId);
            if (application == null || (application.getDeleted() != null && application.getDeleted() == 1)) {
                return;
            }

            // 候选人联系方式
            SysUser candidate = userMapper.selectById(application.getUserId());
            if (candidate != null) {
                detail.put("candidatePhone", candidate.getPhone());
                detail.put("candidateEmail", candidate.getEmail());
                detail.put("candidateAvatarUrl", candidate.getAvatarUrl());
            }

            // 简历记录：优先用投递时绑定的 resumeId，否则取该用户最新简历
            Resume resume = null;
            if (application.getResumeId() != null) {
                resume = resumeMapper.selectById(application.getResumeId());
            }
            if (resume == null && application.getUserId() != null) {
                LambdaQueryWrapper<Resume> rw = new LambdaQueryWrapper<>();
                rw.eq(Resume::getUserId, application.getUserId())
                  .eq(Resume::getDeleted, 0)
                  .orderByDesc(Resume::getCreateTime)
                  .last("LIMIT 1");
                resume = resumeMapper.selectOne(rw);
            }
            if (resume == null) {
                return;
            }

            detail.put("resumeFileUrl", resume.getFileUrl());
            detail.put("selfEvaluation", resume.getSelfEvaluation());

            // 解析简历结构化内容
            if (resume.getParsedContent() != null) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    Map<String, Object> parsedData = mapper.readValue(
                            resume.getParsedContent(),
                            new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                    detail.put("resume", parsedData);
                } catch (Exception e) {
                    log.warn("解析简历内容失败, resumeId={}: {}", resume.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("附加候选人简历信息失败, applicationId={}: {}", applicationId, e.getMessage());
        }
    }

    /**
     * 构建面试记录的通用Map（含关联信息）
     */
    private Map<String, Object> buildInterviewMap(Interview interview) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", interview.getId());
        map.put("interviewTime", interview.getInterviewTime());
        map.put("interviewType", interview.getInterviewType());
        map.put("address", interview.getAddress());
        map.put("status", interview.getStatus());

        // 关联投递 → 候选人 + 职位
        JobApplication application = applicationMapper.selectById(interview.getApplicationId());
        if (application != null) {
            map.put("applicationId", application.getId());
            map.put("candidateId", application.getUserId());
            SysUser candidate = userMapper.selectById(application.getUserId());
            if (candidate != null) {
                map.put("candidateName", candidate.getRealName() != null ? candidate.getRealName() : candidate.getUsername());
            }
            Job job = jobMapper.selectById(application.getJobId());
            if (job != null) {
                map.put("jobName", job.getJobName());
                map.put("jobId", job.getId());
            }
        }

        // 关联面试官
        SysUser interviewer = userMapper.selectById(interview.getInterviewerId());
        if (interviewer != null) {
            map.put("interviewerName", interviewer.getRealName() != null ? interviewer.getRealName() : interviewer.getUsername());
        }

        // 状态文本
        String[] statusLabels = {"待面试", "已完成", "已取消"};
        int s = interview.getStatus() != null ? interview.getStatus() : 0;
        map.put("statusLabel", s >= 0 && s < statusLabels.length ? statusLabels[s] : "未知");

        // 关联面试评价（列表页仅返回评分和结果，不返回详细评价文本）
        if (interview.getStatus() != null && interview.getStatus() == 1) {
            try {
                LambdaQueryWrapper<InterviewEvaluation> evalWrapper = new LambdaQueryWrapper<>();
                evalWrapper.eq(InterviewEvaluation::getInterviewId, interview.getId())
                           .orderByDesc(InterviewEvaluation::getFeedbackTime)
                           .last("LIMIT 1");
                InterviewEvaluation evaluation = evaluationMapper.selectOne(evalWrapper);
                if (evaluation != null) {
                    Map<String, Object> evalMap = new HashMap<>();
                    evalMap.put("technicalScore", evaluation.getTechnicalScore());
                    evalMap.put("communicationScore", evaluation.getCommunicationScore());
                    evalMap.put("logicScore", evaluation.getLogicScore());
                    evalMap.put("overallScore", evaluation.getOverallScore());
                    evalMap.put("result", evaluation.getResult());
                    String[] resultLabels = {"淘汰", "待定", "通过"};
                    int r = evaluation.getResult() != null ? evaluation.getResult() : 0;
                    evalMap.put("resultLabel", r >= 0 && r < resultLabels.length ? resultLabels[r] : "未知");
                    map.put("evaluation", evalMap);
                }
            } catch (Exception e) {
                log.debug("加载面试评价失败, interviewId={}: {}", interview.getId(), e.getMessage());
            }
        }

        return map;
    }

    @Override
    public void saveEvaluation(InterviewEvaluationRequest request, Long interviewerId) {
        Long interviewId = request.getInterviewId();

        // 校验面试记录存在且面试官匹配
        Interview interview = interviewMapper.selectById(interviewId);
        if (interview == null) {
            throw new RuntimeException("面试记录不存在");
        }
        if (!interview.getInterviewerId().equals(interviewerId)) {
            throw new RuntimeException("您不是该面试的面试官，无权提交评价");
        }

        // ── 第一步：先更新面试状态为已完成（独立于评价保存，确保即使评价异常也不阻塞）──
        LambdaQueryWrapper<Interview> statusWrapper = new LambdaQueryWrapper<>();
        statusWrapper.eq(Interview::getId, interviewId);
        Interview updateTarget = new Interview();
        updateTarget.setStatus(1);
        updateTarget.setUpdateTime(LocalDateTime.now());
        int updated = interviewMapper.update(updateTarget, statusWrapper);
        log.info("面试状态已更新, interviewId={}, status→1, 影响行数={}", interviewId, updated);

        // ── 第二步：保存/更新评价（不影响面试状态）──
        try {
            InterviewEvaluation evaluation = new InterviewEvaluation();
            evaluation.setInterviewId(interviewId);
            evaluation.setTechnicalScore(request.getTechnicalScore());
            evaluation.setCommunicationScore(request.getCommunicationScore());
            evaluation.setLogicScore(request.getLogicScore());
            evaluation.setOverallScore(request.getOverallScore());
            evaluation.setEvaluation(request.getEvaluation());
            evaluation.setResult(request.getResult());
            evaluation.setFeedbackTime(LocalDateTime.now());

            // 查询是否已有评价，有则更新，无则插入
            LambdaQueryWrapper<InterviewEvaluation> evalWrapper = new LambdaQueryWrapper<>();
            evalWrapper.eq(InterviewEvaluation::getInterviewId, interviewId)
                       .orderByDesc(InterviewEvaluation::getFeedbackTime)
                       .last("LIMIT 1");
            InterviewEvaluation existing = evaluationMapper.selectOne(evalWrapper);
            if (existing != null) {
                evaluation.setId(existing.getId());
                evaluationMapper.updateById(evaluation);
            } else {
                evaluationMapper.insert(evaluation);
            }
            log.info("面试评价已保存, interviewId={}, result={}", interviewId, evaluation.getResult());
        } catch (Exception e) {
            // 评价保存失败不抛异常——面试状态已经更新完成
            log.error("保存面试评价失败（面试状态已更新）, interviewId={}, error={}", interviewId, e.getMessage(), e);
        }

        // 面试官提交评价后不自动修改投递状态
        // 投递保持为 1(面试中)，HR 在面试详情页点击"录用"或"淘汰"后决定最终状态
    }

    @Override
    public Map<String, Object> getEvaluationByInterviewId(Long interviewId) {
        LambdaQueryWrapper<InterviewEvaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InterviewEvaluation::getInterviewId, interviewId)
               .orderByDesc(InterviewEvaluation::getFeedbackTime)
               .last("LIMIT 1");
        InterviewEvaluation evaluation = evaluationMapper.selectOne(wrapper);
        if (evaluation == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("id", evaluation.getId());
        map.put("interviewId", evaluation.getInterviewId());
        map.put("technicalScore", evaluation.getTechnicalScore());
        map.put("communicationScore", evaluation.getCommunicationScore());
        map.put("logicScore", evaluation.getLogicScore());
        map.put("overallScore", evaluation.getOverallScore());
        map.put("evaluation", evaluation.getEvaluation());
        map.put("result", evaluation.getResult());
        map.put("feedbackTime", evaluation.getFeedbackTime());
        String[] resultLabels = {"淘汰", "待定", "通过"};
        int r = evaluation.getResult() != null ? evaluation.getResult() : 0;
        map.put("resultLabel", r >= 0 && r < resultLabels.length ? resultLabels[r] : "未知");
        return map;
    }

    @Override
    public void cancelInterview(Long interviewId, Long operatorId) {
        Interview interview = interviewMapper.selectById(interviewId);
        if (interview == null) {
            throw new RuntimeException("面试记录不存在");
        }
        if (interview.getStatus() != 0) {
            throw new RuntimeException("只能取消待面试状态的面试");
        }
        interview.setStatus(2); // 已取消
        interview.setUpdateTime(LocalDateTime.now());
        interviewMapper.updateById(interview);

        // 恢复投递状态为待筛选(0)
        JobApplication application = applicationMapper.selectById(interview.getApplicationId());
        if (application != null && application.getStatus() == 1) {
            application.setStatus(0);
            application.setUpdateTime(LocalDateTime.now());
            applicationMapper.updateById(application);
        }
    }

    @Override
    public void processResult(Long interviewId, Integer hireDecision, Long operatorId,
                               String emailSubject, String emailBody) {
        Interview interview = interviewMapper.selectById(interviewId);
        if (interview == null) {
            throw new RuntimeException("面试记录不存在");
        }
        if (interview.getStatus() != 1) {
            throw new RuntimeException("只能处理已完成的面试");
        }

        JobApplication application = applicationMapper.selectById(interview.getApplicationId());
        if (application == null) {
            throw new RuntimeException("投递记录不存在");
        }

        // 加载候选人邮箱和名称
        String candidateEmail = null;
        String candidateName = null;
        SysUser candidate = userMapper.selectById(application.getUserId());
        if (candidate != null) {
            candidateEmail = candidate.getEmail();
            candidateName = candidate.getRealName() != null ? candidate.getRealName() : candidate.getUsername();
        }
        String jobName = null;
        Job job = jobMapper.selectById(application.getJobId());
        if (job != null) {
            jobName = job.getJobName();
        }

        // hireDecision: 1-录用, 0-淘汰
        if (hireDecision != null && hireDecision == 1) {
            application.setStatus(2); // 已录用
            mailService.sendHireNotification(candidateEmail, candidateName, jobName,
                    interview.getInterviewTime() != null ? interview.getInterviewTime().toString() : null,
                    emailSubject, emailBody);
        } else {
            application.setStatus(3); // 不合适
            mailService.sendRejectNotification(candidateEmail, candidateName, jobName,
                    emailSubject, emailBody);
        }
        application.setUpdateTime(LocalDateTime.now());
        applicationMapper.updateById(application);
    }

    /**
     * 发送面试邀请邮件（异步），失败不影响主流程
     */
    private void sendInterviewInvitationEmail(JobApplication application, LocalDateTime interviewTime,
                                               Long interviewerId, String address) {
        try {
            String candidateEmail = null;
            String candidateName = null;
            SysUser candidate = userMapper.selectById(application.getUserId());
            if (candidate != null) {
                candidateEmail = candidate.getEmail();
                candidateName = candidate.getRealName() != null ? candidate.getRealName() : candidate.getUsername();
            }
            String jobName = null;
            Job job = jobMapper.selectById(application.getJobId());
            if (job != null) {
                jobName = job.getJobName();
            }
            String interviewerName = null;
            SysUser interviewer = userMapper.selectById(interviewerId);
            if (interviewer != null) {
                interviewerName = interviewer.getRealName() != null ? interviewer.getRealName() : interviewer.getUsername();
            }
            mailService.sendInterviewInvitation(candidateEmail, candidateName, jobName,
                    interviewTime != null ? interviewTime.toString() : null, interviewerName, address);
        } catch (Exception e) {
            log.warn("发送面试邀请邮件失败，不影响面试创建: applicationId={}, error={}",
                    application.getId(), e.getMessage());
        }
    }
}
