package com.recruitment.application.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruitment.application.entity.JobApplication;
import com.recruitment.application.mapper.JobApplicationMapper;
import com.recruitment.application.service.ApplicationService;
import com.recruitment.common.core.model.PageResult;
import com.recruitment.job.entity.Job;
import com.recruitment.job.mapper.JobMapper;
import com.recruitment.resume.entity.Resume;
import com.recruitment.resume.mapper.ResumeMapper;
import com.recruitment.system.entity.SysUser;
import com.recruitment.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
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
    private final SysUserMapper userMapper;

    @Override
    @Transactional
    public Map<String, Object> apply(Long userId, Long jobId) {
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

        // 查找用户简历
        LambdaQueryWrapper<Resume> resumeWrapper = new LambdaQueryWrapper<>();
        resumeWrapper.eq(Resume::getUserId, userId)
                     .eq(Resume::getDeleted, 0)
                     .orderByDesc(Resume::getCreateTime)
                     .last("LIMIT 1");
        Resume resume = resumeMapper.selectOne(resumeWrapper);

        // 创建投递记录
        JobApplication application = new JobApplication();
        application.setJobId(jobId);
        application.setUserId(userId);
        application.setResumeId(resume != null ? resume.getId() : null);
        application.setApplyTime(LocalDateTime.now());
        application.setStatus(0); // 待筛选
        application.setDeleted(0);
        applicationMapper.insert(application);

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

            return map;
        }).collect(Collectors.toList());

        return new PageResult<>(records, resultPage.getTotal(), pageNum, pageSize);
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
