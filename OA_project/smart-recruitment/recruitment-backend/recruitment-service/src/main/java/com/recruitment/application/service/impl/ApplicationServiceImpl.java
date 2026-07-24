package com.recruitment.application.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruitment.application.entity.JobApplication;
import com.recruitment.application.mapper.JobApplicationMapper;
import com.recruitment.application.service.ApplicationService;
import com.recruitment.common.core.model.PageResult;
import com.recruitment.common.redis.util.DailyStatsCounter;
import com.recruitment.interview.entity.Interview;
import com.recruitment.interview.mapper.InterviewMapper;
import com.recruitment.job.entity.Job;
import com.recruitment.job.mapper.JobMapper;
import com.recruitment.resume.entity.Resume;
import com.recruitment.resume.mapper.ResumeMapper;
import com.recruitment.resume.service.ResumeService;
import com.recruitment.system.entity.SysUser;
import com.recruitment.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 投递服务实现
 */
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final JobApplicationMapper applicationMapper;
    private final JobMapper jobMapper;
    private final ResumeMapper resumeMapper;
    private final ResumeService resumeService;
    private final SysUserMapper userMapper;
    private final InterviewMapper interviewMapper;
    private final ObjectProvider<DailyStatsCounter> dailyStatsCounterProvider;

    /** 今日投递数计数器业务域 */
    private static final String DOMAIN_APPLICATION = "application";

    @Override
    @Transactional
    public Map<String, Object> apply(Long userId, Long jobId, Long resumeId) {
        // 检查职位是否存在
        Job job = jobMapper.selectById(jobId);
        if (job == null) {
            throw new RuntimeException("职位不存在");
        }
        if (job.getStatus() != 1) {
            throw new RuntimeException("该职位未开放投递");
        }

        // 检查是否已投递
        LambdaQueryWrapper<JobApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobApplication::getJobId, jobId)
               .eq(JobApplication::getUserId, userId)
               .eq(JobApplication::getDeleted, 0);
        JobApplication existing = applicationMapper.selectOne(wrapper);
        if (existing != null) {
            throw new RuntimeException("您已投递过该职位，请勿重复投递");
        }

        // 未指定简历时，自动查找用户最新简历
        if (resumeId == null) {
            LambdaQueryWrapper<Resume> resumeWrapper = new LambdaQueryWrapper<>();
            resumeWrapper.eq(Resume::getUserId, userId)
                         .eq(Resume::getDeleted, 0)
                         .orderByDesc(Resume::getCreateTime)
                         .last("LIMIT 1");
            Resume resume = resumeMapper.selectOne(resumeWrapper);
            resumeId = resume != null ? resume.getId() : null;
        }

        // 创建投递记录
        JobApplication application = new JobApplication();
        application.setJobId(jobId);
        application.setUserId(userId);
        application.setResumeId(resumeId);
        application.setApplyTime(LocalDateTime.now());
        application.setStatus(0); // 待筛选
        application.setDeleted(0);
        applicationMapper.insert(application);

        // Redis INCR 实时统计今日投递数，比 COUNT(*) 效率高得多
        DailyStatsCounter counter = dailyStatsCounterProvider.getIfAvailable();
        if (counter != null) {
            counter.incrementToday(DOMAIN_APPLICATION);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", application.getId());
        result.put("jobId", jobId);
        result.put("jobName", job.getJobName());
        result.put("status", 0);
        return result;
    }

    @Override
    public PageResult<Map<String, Object>> listApplications(long pageNum, long pageSize, Long userId, Integer status, String keyword) {
        LambdaQueryWrapper<JobApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(JobApplication::getDeleted, 0);

        if (userId != null) {
            wrapper.eq(JobApplication::getUserId, userId);
        }
        if (status != null) {
            wrapper.eq(JobApplication::getStatus, status);
        }

        wrapper.orderByDesc(JobApplication::getCreateTime);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<JobApplication> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<JobApplication> resultPage = applicationMapper.selectPage(page, wrapper);

        // 关联查询职位和用户信息
        List<Map<String, Object>> records = resultPage.getRecords().stream().map(app -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", app.getId());
            map.put("jobId", app.getJobId());
            map.put("userId", app.getUserId());
            map.put("resumeId", app.getResumeId());
            map.put("status", app.getStatus());
            map.put("aiMatchScore", app.getAiMatchScore());
            map.put("aiMatchReason", app.getAiMatchReason());
            map.put("remark", app.getRemark());
            map.put("createTime", app.getCreateTime());
            map.put("applyTime", app.getApplyTime());

            // 关联职位信息
            Job job = jobMapper.selectById(app.getJobId());
            if (job != null) {
                map.put("jobName", job.getJobName());
                map.put("department", job.getDepartment());
                map.put("city", job.getCity());
                map.put("category", job.getCategory());
            }

            // 关联用户信息
            SysUser user = userMapper.selectById(app.getUserId());
            if (user != null) {
                map.put("candidateName", user.getRealName() != null ? user.getRealName() : user.getUsername());
                map.put("userName", user.getUsername());
            }

            // 关联面试信息
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Interview> interviewWrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            interviewWrapper.eq(Interview::getApplicationId, app.getId())
                    .orderByDesc(Interview::getCreateTime)
                    .last("LIMIT 1");
            Interview latestInterview = interviewMapper.selectOne(interviewWrapper);
            if (latestInterview != null) {
                map.put("interviewId", latestInterview.getId());
                map.put("interviewStatus", latestInterview.getStatus());
            } else {
                map.put("interviewId", null);
                map.put("interviewStatus", null);
            }

            return map;
        }).collect(Collectors.toList());

        return new PageResult<>(records, resultPage.getTotal(), pageNum, pageSize);
    }

    @Override
    public Map<String, Object> getCandidateDetail(Long applicationId) {
        JobApplication application = applicationMapper.selectById(applicationId);
        if (application == null || application.getDeleted() == 1) {
            throw new RuntimeException("投递记录不存在");
        }

        Map<String, Object> detail = new HashMap<>();

        // 投递信息
        detail.put("id", application.getId());
        detail.put("jobId", application.getJobId());
        detail.put("userId", application.getUserId());
        detail.put("resumeId", application.getResumeId());
        detail.put("status", application.getStatus());
        detail.put("applyTime", application.getApplyTime());
        detail.put("aiMatchScore", application.getAiMatchScore());
        detail.put("aiMatchReason", application.getAiMatchReason());

        // 职位信息
        Job job = jobMapper.selectById(application.getJobId());
        if (job != null) {
            detail.put("jobName", job.getJobName());
            detail.put("department", job.getDepartment());
            detail.put("category", job.getCategory());
        }

        // 候选人用户信息
        SysUser user = userMapper.selectById(application.getUserId());
        if (user != null) {
            detail.put("candidateName", user.getRealName() != null ? user.getRealName() : user.getUsername());
            detail.put("username", user.getUsername());
            detail.put("email", user.getEmail());
            detail.put("phone", user.getPhone());
            detail.put("avatarUrl", user.getAvatarUrl());
        }

        // 简历数据
        if (application.getResumeId() != null) {
            Resume resume = resumeMapper.selectById(application.getResumeId());
            if (resume != null) {
                detail.put("resumeFileUrl", resume.getFileUrl());
                detail.put("selfEvaluation", resume.getSelfEvaluation());
                // 解析JSON格式的简历内容
                if (resume.getParsedContent() != null) {
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        Map<String, Object> parsedData = mapper.readValue(resume.getParsedContent(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                        detail.put("resume", parsedData);
                    } catch (Exception e) {
                        detail.put("resume", null);
                    }
                }
            }
        } else if (application.getUserId() != null) {
            // 没有关联简历记录，尝试通过userId获取
            try {
                Map<String, Object> resumeData = resumeService.getMyResume(application.getUserId());
                detail.put("resume", resumeData);
            } catch (Exception e) {
                detail.put("resume", null);
            }
        }

        return detail;
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status, Long operatorId) {
        JobApplication application = applicationMapper.selectById(id);
        if (application == null || application.getDeleted() == 1) {
            throw new RuntimeException("投递记录不存在");
        }

        application.setStatus(status);
        application.setUpdateTime(LocalDateTime.now());
        applicationMapper.updateById(application);
    }
}
