package com.recruitment.job.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruitment.common.redis.util.RedisUtil;
import com.recruitment.job.entity.Job;
import com.recruitment.job.mapper.JobMapper;
import com.recruitment.job.service.RecommendationService;
import com.recruitment.job.vo.JobRecommendVO;
import com.recruitment.resume.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 基于技能标签重叠度的职位推荐实现。
 * <p>
 * 匹配分 = 用户技能与职位技能的交集大小 / 职位技能数（覆盖率，0~100），
 * 即“用户满足该职位技能要求的比例”。按匹配分降序、命中数降序、发布时间降序排序。
 * 结果按用户缓存到 Redis，TTL 5 分钟；职位写操作会清除所有用户推荐缓存。
 * 用户无简历或无技能时回退为最新在招职位。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private static final String CACHE_PREFIX = "job:recommend:";
    private static final long CACHE_TTL_MINUTES = 5;
    /** 候选池大小：取最近 200 条在招职位参与打分，兼顾覆盖度与性能 */
    private static final int CANDIDATE_POOL_SIZE = 200;

    private final JobMapper jobMapper;
    private final ResumeService resumeService;
    private final ObjectProvider<RedisUtil> redisUtilProvider;

    @Override
    @SuppressWarnings("unchecked")
    public List<JobRecommendVO> recommendJobs(Long userId, int limit) {
        if (userId == null || limit <= 0) {
            return Collections.emptyList();
        }

        String cacheKey = CACHE_PREFIX + userId;
        List<JobRecommendVO> cached = readCache(cacheKey);
        if (cached != null) {
            return cached.size() > limit ? cached.subList(0, limit) : cached;
        }

        List<Job> pool = loadCandidatePool();

        // 收集候选池中所有职位技能，作为“已知技能词表”，用于从简历文本中回退识别
        Set<String> knownJobSkills = pool.stream()
                .flatMap(job -> parseSkills(job.getSkills()).stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<String> userSkills = extractUserSkills(userId, knownJobSkills);
        log.info("Recommendation for user {}: extracted {} skill(s) = {}", userId, userSkills.size(), userSkills);

        List<JobRecommendVO> ranked = pool.stream()
                .map(job -> scoreJob(job, userSkills))
                .sorted(Comparator
                        .comparingDouble(JobRecommendVO::getMatchScore).reversed()
                        .thenComparing(Comparator
                                .comparing((JobRecommendVO j) -> j.getMatchedSkills() == null ? 0 : j.getMatchedSkills().size())
                                .reversed())
                        .thenComparing(Job::getCreateTime,
                                Comparator.nullsFirst(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        writeCache(cacheKey, ranked);
        return ranked.size() > limit ? ranked.subList(0, limit) : ranked;
    }

    private Set<String> extractUserSkills(Long userId, Set<String> knownJobSkills) {
        Map<String, Object> resume;
        try {
            resume = resumeService.getMyResume(userId);
        } catch (RuntimeException e) {
            log.warn("Failed to load resume of user {}: {}", userId, e.getMessage());
            return Collections.emptySet();
        }

        Set<String> skills = new LinkedHashSet<>();

        // 1. 优先读取简历中显式声明的技能标签数组
        Object raw = resume.get("skills");
        if (raw instanceof List<?> list) {
            skills.addAll(normalizeSkills(list.stream().map(String::valueOf)));
        } else if (raw instanceof String s && StringUtils.hasText(s)) {
            skills.addAll(normalizeSkills(Arrays.stream(s.split("[,，\\n]"))));
        }

        // 2. 若技能标签为空，则回退：用已知职位技能词表在简历全文中做包含匹配
        if (skills.isEmpty() && !knownJobSkills.isEmpty()) {
            String text = collectResumeText(resume);
            if (StringUtils.hasText(text)) {
                String lower = text.toLowerCase();
                for (String sk : knownJobSkills) {
                    if (lower.contains(sk)) {
                        skills.add(sk);
                    }
                }
            }
        }
        return skills;
    }

    /**
     * 将简历 Map 中的字符串字段（姓名、自我评价、摘要等）拼接成一段文本，用于技能关键词回退匹配。
     */
    private String collectResumeText(Map<String, Object> resume) {
        StringBuilder sb = new StringBuilder();
        for (Object value : resume.values()) {
            if (value instanceof String s && StringUtils.hasText(s)) {
                sb.append(s).append(' ');
            }
        }
        return sb.toString();
    }

    private Set<String> parseSkills(String skills) {
        return normalizeSkills(Arrays.stream(String.valueOf(skills).split("[,，\\n]")));
    }

    private Set<String> normalizeSkills(java.util.stream.Stream<String> stream) {
        return stream.map(String::trim)
                .filter(StringUtils::hasText)
                .map(String::toLowerCase)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private List<Job> loadCandidatePool() {
        LambdaQueryWrapper<Job> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Job::getStatus, 1)
                .eq(Job::getDeleted, 0)
                .orderByDesc(Job::getCreateTime)
                .last("LIMIT " + CANDIDATE_POOL_SIZE);
        return jobMapper.selectList(wrapper);
    }

    private JobRecommendVO scoreJob(Job job, Set<String> userSkills) {
        JobRecommendVO vo = new JobRecommendVO();
        BeanUtils.copyProperties(job, vo);

        Set<String> jobSkills = parseSkills(job.getSkills());

        if (userSkills.isEmpty() || jobSkills.isEmpty()) {
            vo.setMatchScore(0.0);
            vo.setMatchedSkills(Collections.emptyList());
            return vo;
        }

        List<String> matched = jobSkills.stream()
                .filter(userSkills::contains)
                .collect(Collectors.toList());

        double score = matched.size() * 100.0 / jobSkills.size();
        vo.setMatchScore(Math.round(score * 10) / 10.0);
        vo.setMatchedSkills(matched);
        return vo;
    }

    @SuppressWarnings("unchecked")
    private List<JobRecommendVO> readCache(String cacheKey) {
        RedisUtil redisUtil = redisUtilProvider.getIfAvailable();
        if (redisUtil == null) {
            return null;
        }
        try {
            Object cached = redisUtil.get(cacheKey);
            if (cached instanceof List<?> list) {
                return (List<JobRecommendVO>) list;
            }
        } catch (RuntimeException e) {
            log.warn("Failed to read recommendation cache {}: {}", cacheKey, e.getMessage());
        }
        return null;
    }

    private void writeCache(String cacheKey, List<JobRecommendVO> value) {
        RedisUtil redisUtil = redisUtilProvider.getIfAvailable();
        if (redisUtil == null || value == null) {
            return;
        }
        try {
            redisUtil.set(cacheKey, value, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (RuntimeException e) {
            log.warn("Failed to write recommendation cache {}: {}", cacheKey, e.getMessage());
        }
    }
}
