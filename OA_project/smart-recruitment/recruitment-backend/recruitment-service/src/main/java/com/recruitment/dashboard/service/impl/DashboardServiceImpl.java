package com.recruitment.dashboard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruitment.application.entity.JobApplication;
import com.recruitment.application.mapper.JobApplicationMapper;
import com.recruitment.common.redis.util.RedisUtil;
import com.recruitment.dashboard.service.DashboardService;
import com.recruitment.dashboard.vo.DashboardStatsVO;
import com.recruitment.interview.entity.Interview;
import com.recruitment.interview.mapper.InterviewMapper;
import com.recruitment.job.entity.Job;
import com.recruitment.job.mapper.JobMapper;
import com.recruitment.system.entity.SysUser;
import com.recruitment.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 仪表板服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Profile("!local")
public class DashboardServiceImpl implements DashboardService {

    private final JobMapper jobMapper;
    private final JobApplicationMapper jobApplicationMapper;
    private final InterviewMapper interviewMapper;
    private final SysUserMapper sysUserMapper;
    private final RedisUtil redisUtil;

    /** Dashboard 统计数据 Redis 缓存 key */
    private static final String DASHBOARD_STATS_CACHE_KEY = "stats:dashboard";
    /** 缓存 TTL：5 分钟 */
    private static final long DASHBOARD_STATS_CACHE_TTL_MINUTES = 5;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public DashboardStatsVO getStats() {
        // 1. 尝试从 Redis 缓存读取
        DashboardStatsVO cached = getCachedStats();
        if (cached != null) {
            log.debug("Dashboard stats cache hit");
            return cached;
        }

        DashboardStatsVO vo = new DashboardStatsVO();

        // 2. 智能在招岗位数：status = 1（招聘中）
        vo.setActiveJobs(jobMapper.selectCount(
                new LambdaQueryWrapper<Job>()
                        .eq(Job::getStatus, 1)
                        .eq(Job::getDeleted, 0)));

        // 3. 系统累计投递数
        vo.setTotalApplications(jobApplicationMapper.selectCount(
                new LambdaQueryWrapper<JobApplication>()
                        .eq(JobApplication::getDeleted, 0)));

        // 4. 日常面试中：interview status = 0（待面试）
        vo.setInterviewing(interviewMapper.selectCount(
                new LambdaQueryWrapper<Interview>()
                        .eq(Interview::getStatus, 0)));

        // 5. 本月入职新员工：job_application status = 2（录用）且 update_time 在本月
        LocalDate now = LocalDate.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = now.plusMonths(1).withDayOfMonth(1).atStartOfDay();
        vo.setOnboardingThisMonth(jobApplicationMapper.selectCount(
                new LambdaQueryWrapper<JobApplication>()
                        .eq(JobApplication::getStatus, 2)
                        .eq(JobApplication::getDeleted, 0)
                        .ge(JobApplication::getUpdateTime, monthStart)
                        .lt(JobApplication::getUpdateTime, monthEnd)));

        // 6. 清理旧版 Redis 缓存 key（stats:application:daily:* / stats:interview:daily:*）
        cleanupLegacyStatsKeys();

        // 7. 投递阶段漏斗占比
        List<JobApplication> allApps = jobApplicationMapper.selectList(
                new LambdaQueryWrapper<JobApplication>()
                        .eq(JobApplication::getDeleted, 0));
        Map<Integer, Long> statusCountMap = allApps.stream()
                .collect(Collectors.groupingBy(JobApplication::getStatus, Collectors.counting()));

        List<DashboardStatsVO.ProgressItem> progressData = new ArrayList<>();
        progressData.add(new DashboardStatsVO.ProgressItem("待筛选",
                statusCountMap.getOrDefault(0, 0L)));
        progressData.add(new DashboardStatsVO.ProgressItem("面试中",
                statusCountMap.getOrDefault(1, 0L)));
        progressData.add(new DashboardStatsVO.ProgressItem("已录用",
                statusCountMap.getOrDefault(2, 0L)));
        progressData.add(new DashboardStatsVO.ProgressItem("已淘汰",
                statusCountMap.getOrDefault(3, 0L)));
        vo.setProgressData(progressData);

        // 6. 最近6个月投递趋势，包含无投递月份
        YearMonth currentMonth = YearMonth.from(now);
        YearMonth firstMonth = currentMonth.minusMonths(5);
        LocalDateTime trendStart = firstMonth.atDay(1).atStartOfDay();
        Map<YearMonth, Long> monthlyCountMap = allApps.stream()
                .map(this::getApplicationTime)
                .filter(time -> time != null && !time.isBefore(trendStart))
                .collect(Collectors.groupingBy(YearMonth::from, Collectors.counting()));

        Map<YearMonth, Long> orderedMonthlyCounts = new LinkedHashMap<>();
        for (int i = 0; i < 6; i++) {
            YearMonth month = firstMonth.plusMonths(i);
            orderedMonthlyCounts.put(month, monthlyCountMap.getOrDefault(month, 0L));
        }
        vo.setMonthlyTrend(orderedMonthlyCounts.entrySet().stream()
                .map(entry -> new DashboardStatsVO.MonthlyTrendItem(
                        entry.getKey().getMonthValue() + "月",
                        entry.getValue()))
                .collect(Collectors.toList()));

        // 7. 最新投递记录（前10条）
        List<JobApplication> recentApps = jobApplicationMapper.selectList(
                new LambdaQueryWrapper<JobApplication>()
                        .eq(JobApplication::getDeleted, 0)
                        .orderByDesc(JobApplication::getCreateTime)
                        .last("LIMIT 10"));

        // 批量获取关联的 job 和 user
        List<Long> jobIds = recentApps.stream()
                .map(JobApplication::getJobId).distinct().collect(Collectors.toList());
        List<Long> userIds = recentApps.stream()
                .map(JobApplication::getUserId).distinct().collect(Collectors.toList());

        final Map<Long, String> jobNameMap;
        if (!jobIds.isEmpty()) {
            jobNameMap = jobMapper.selectBatchIds(jobIds).stream()
                    .filter(j -> j != null)
                    .collect(Collectors.toMap(Job::getId, Job::getJobName, (a, b) -> a));
        } else {
            jobNameMap = Map.of();
        }

        final Map<Long, String> userNameMap;
        if (!userIds.isEmpty()) {
            userNameMap = sysUserMapper.selectBatchIds(userIds).stream()
                    .filter(u -> u != null)
                    .collect(Collectors.toMap(SysUser::getId,
                            u -> u.getRealName() != null ? u.getRealName() : u.getUsername(),
                            (a, b) -> a));
        } else {
            userNameMap = Map.of();
        }

        List<DashboardStatsVO.RecentApplication> recentList = recentApps.stream().map(app -> {
            DashboardStatsVO.RecentApplication ra = new DashboardStatsVO.RecentApplication();
            ra.setCandidateName(userNameMap.getOrDefault(app.getUserId(), "未知"));
            ra.setJobTitle(jobNameMap.getOrDefault(app.getJobId(), "未知"));
            BigDecimal score = app.getAiMatchScore();
            ra.setResumeScore(score != null ? score.intValue() : 0);
            ra.setStatus(convertStatus(app.getStatus()));
            ra.setApplyTime(app.getApplyTime() != null
                    ? app.getApplyTime().format(DATE_FMT) : "");
            return ra;
        }).collect(Collectors.toList());
        vo.setRecentApplications(recentList);

        // 8. 写入 Redis 缓存（完整 VO）
        cacheStats(vo);

        return vo;
    }

    @Override
    public DashboardStatsVO getInterviewerStats(Long interviewerId) {
        DashboardStatsVO vo = new DashboardStatsVO();

        // 该面试官待面试数
        vo.setInterviewing(interviewMapper.selectCount(
                new LambdaQueryWrapper<Interview>()
                        .eq(Interview::getInterviewerId, interviewerId)
                        .eq(Interview::getStatus, 0)));

        // 该面试官已完成面试数
        vo.setActiveJobs(interviewMapper.selectCount(
                new LambdaQueryWrapper<Interview>()
                        .eq(Interview::getInterviewerId, interviewerId)
                        .eq(Interview::getStatus, 1)));

        // 本月面试数
        LocalDate now = LocalDate.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = now.plusMonths(1).withDayOfMonth(1).atStartOfDay();
        vo.setTotalApplications(interviewMapper.selectCount(
                new LambdaQueryWrapper<Interview>()
                        .eq(Interview::getInterviewerId, interviewerId)
                        .ge(Interview::getCreateTime, monthStart)
                        .lt(Interview::getCreateTime, monthEnd)));

        // 通过率：已完成中…简化处理
        long completed = vo.getActiveJobs() != null ? vo.getActiveJobs() : 0;
        long total = (vo.getInterviewing() != null ? vo.getInterviewing() : 0) + completed;
        // 通过率用 onboardingThisMonth 字段传回（前端展示用）
        vo.setOnboardingThisMonth(total > 0 ? Math.round((double) completed / total * 100) : 0L);

        vo.setProgressData(List.of());
        vo.setMonthlyTrend(List.of());
        vo.setRecentApplications(List.of());
        return vo;
    }

    /**
     * 从 Redis 读取 Dashboard 统计数据缓存
     */
    private DashboardStatsVO getCachedStats() {
        try {
            Object cached = redisUtil.get(DASHBOARD_STATS_CACHE_KEY);
            if (cached instanceof DashboardStatsVO vo) {
                return vo;
            }
        } catch (RuntimeException e) {
            log.warn("Failed to read dashboard stats from Redis cache: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 将 Dashboard 统计数据写入 Redis 缓存
     */
    private void cacheStats(DashboardStatsVO vo) {
        try {
            redisUtil.set(DASHBOARD_STATS_CACHE_KEY, vo, DASHBOARD_STATS_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (RuntimeException e) {
            log.warn("Failed to write dashboard stats to Redis cache: {}", e.getMessage());
        }
    }

    /**
     * 清理旧版 Redis 缓存 key（stats:application:daily:* / stats:interview:daily:*）
     */
    private void cleanupLegacyStatsKeys() {
        try {
            long deleted = redisUtil.deleteByPattern("stats:application:daily:*");
            deleted += redisUtil.deleteByPattern("stats:interview:daily:*");
            if (deleted > 0) {
                log.info("Cleaned up {} legacy daily stats Redis keys", deleted);
            }
        } catch (RuntimeException e) {
            log.warn("Failed to cleanup legacy stats keys: {}", e.getMessage());
        }
    }

    /**
     * 将数字状态码转为前端期望的英文字符串
     * 0-待筛选→PENDING  1-面试中→INTERVIEW  2-录用→OFFER  3-淘汰→REJECTED
     */
    private String convertStatus(Integer status) {
        if (status == null) return "PENDING";
        return switch (status) {
            case 1 -> "INTERVIEW";
            case 2 -> "OFFER";
            case 3 -> "REJECTED";
            default -> "PENDING";
        };
    }

    private LocalDateTime getApplicationTime(JobApplication application) {
        if (application.getApplyTime() != null) {
            return application.getApplyTime();
        }
        return application.getCreateTime();
    }
}
