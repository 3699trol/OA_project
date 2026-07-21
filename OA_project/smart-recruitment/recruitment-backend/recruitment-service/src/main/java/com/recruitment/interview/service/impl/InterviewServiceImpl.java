package com.recruitment.interview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruitment.application.entity.JobApplication;
import com.recruitment.application.mapper.JobApplicationMapper;
import com.recruitment.common.core.model.PageResult;
import com.recruitment.interview.entity.Interview;
import com.recruitment.interview.entity.InterviewEvaluation;
import com.recruitment.interview.mapper.InterviewEvaluationMapper;
import com.recruitment.interview.mapper.InterviewMapper;
import com.recruitment.interview.service.InterviewService;
import com.recruitment.job.entity.Job;
import com.recruitment.job.mapper.JobMapper;
import com.recruitment.system.entity.SysUser;
import com.recruitment.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 面试服务实现
 */
@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {

    private final InterviewMapper interviewMapper;
    private final InterviewEvaluationMapper evaluationMapper;
    private final JobApplicationMapper applicationMapper;
    private final JobMapper jobMapper;
    private final SysUserMapper userMapper;

    @Override
    public Map<String, Object> createInterview(Map<String, Object> params, Long operatorId) {
        // 必填参数校验
        Object applicationIdObj = params.get("applicationId");
        Object interviewerIdObj = params.get("interviewerId");
        Object interviewTimeObj = params.get("interviewTime");

        if (applicationIdObj == null) {
            throw new RuntimeException("请选择候选人（投递记录）");
        }
        if (interviewerIdObj == null) {
            throw new RuntimeException("请选择面试官");
        }
        if (interviewTimeObj == null || interviewTimeObj.toString().isEmpty()) {
            throw new RuntimeException("请选择面试时间");
        }

        Long applicationId = Long.valueOf(applicationIdObj.toString());
        Long interviewerId = Long.valueOf(interviewerIdObj.toString());

        // 校验投递记录存在
        JobApplication application = applicationMapper.selectById(applicationId);
        if (application == null || application.getDeleted() == 1) {
            throw new RuntimeException("投递记录不存在");
        }

        // 解析面试时间
        LocalDateTime interviewTime;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            interviewTime = LocalDateTime.parse(interviewTimeObj.toString(), formatter);
        } catch (Exception e) {
            throw new RuntimeException("面试时间格式错误，请使用 yyyy-MM-dd HH:mm 格式");
        }

        // 构建面试类型（合并 type + mode）
        String type = params.get("type") != null ? params.get("type").toString() : "技术面试";
        String mode = params.get("mode") != null ? params.get("mode").toString() : "线上";
        String interviewType = type + "-" + mode;

        // 地址/备注
        String address = params.get("address") != null ? params.get("address").toString() : null;

        // 创建面试记录
        Interview interview = new Interview();
        interview.setApplicationId(applicationId);
        interview.setInterviewerId(interviewerId);
        interview.setInterviewTime(interviewTime);
        interview.setInterviewType(interviewType);
        interview.setAddress(address);
        interview.setStatus(0); // 待面试
        interviewMapper.insert(interview);

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
    public Map<String, Object> getInterviewDetail(Long interviewId) {
        Interview interview = interviewMapper.selectById(interviewId);
        if (interview == null) {
            throw new RuntimeException("面试记录不存在");
        }
        Map<String, Object> detail = buildInterviewMap(interview);

        // 关联面试评价
        LambdaQueryWrapper<InterviewEvaluation> evalWrapper = new LambdaQueryWrapper<>();
        evalWrapper.eq(InterviewEvaluation::getInterviewId, interviewId);
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

        return detail;
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
    }

    @Override
    public void saveEvaluation(Map<String, Object> params, Long interviewerId) {
        Object interviewIdObj = params.get("interviewId");
        if (interviewIdObj == null) {
            throw new RuntimeException("面试ID不能为空");
        }
        Long interviewId = Long.valueOf(interviewIdObj.toString());

        // 校验面试记录存在且面试官匹配
        Interview interview = interviewMapper.selectById(interviewId);
        if (interview == null) {
            throw new RuntimeException("面试记录不存在");
        }
        if (!interview.getInterviewerId().equals(interviewerId)) {
            throw new RuntimeException("您不是该面试的面试官，无权提交评价");
        }

        // 构建评价对象
        InterviewEvaluation evaluation = new InterviewEvaluation();
        evaluation.setInterviewId(interviewId);
        if (params.get("technicalScore") != null) {
            evaluation.setTechnicalScore(Integer.valueOf(params.get("technicalScore").toString()));
        }
        if (params.get("communicationScore") != null) {
            evaluation.setCommunicationScore(Integer.valueOf(params.get("communicationScore").toString()));
        }
        if (params.get("logicScore") != null) {
            evaluation.setLogicScore(Integer.valueOf(params.get("logicScore").toString()));
        }
        if (params.get("overallScore") != null) {
            evaluation.setOverallScore(Integer.valueOf(params.get("overallScore").toString()));
        }
        evaluation.setEvaluation(params.get("evaluation") != null ? params.get("evaluation").toString() : null);
        if (params.get("result") != null) {
            evaluation.setResult(Integer.valueOf(params.get("result").toString()));
        }
        evaluation.setFeedbackTime(LocalDateTime.now());

        // 查询是否已有评价，有则更新，无则插入
        LambdaQueryWrapper<InterviewEvaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InterviewEvaluation::getInterviewId, interviewId);
        InterviewEvaluation existing = evaluationMapper.selectOne(wrapper);
        if (existing != null) {
            evaluation.setId(existing.getId());
            evaluationMapper.updateById(evaluation);
        } else {
            evaluationMapper.insert(evaluation);
        }

        // 更新面试状态为已完成
        interview.setStatus(1);
        interview.setUpdateTime(LocalDateTime.now());
        interviewMapper.updateById(interview);

        // 注意：面试官提交评价后，不自动修改投递状态
        // 投递状态保持为 1(面试中)，由 HR 在面试详情页点击"录用"或"淘汰"后决定最终状态
    }

    @Override
    public Map<String, Object> getEvaluationByInterviewId(Long interviewId) {
        LambdaQueryWrapper<InterviewEvaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InterviewEvaluation::getInterviewId, interviewId);
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
    public void processResult(Long interviewId, Integer hireDecision, Long operatorId) {
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

        // hireDecision: 1-录用, 0-淘汰
        if (hireDecision != null && hireDecision == 1) {
            application.setStatus(2); // 已录用
        } else {
            application.setStatus(3); // 不合适
        }
        application.setUpdateTime(LocalDateTime.now());
        applicationMapper.updateById(application);
    }
}
