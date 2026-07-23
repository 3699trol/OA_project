package com.recruitment.common.redis.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis INCR 的按日实时计数器，用于 HR 工作台“今日投递数 / 今日面试数”等指标。
 * <p>
 * Key 形如 {@code stats:application:20260723}，每日自增；首次自增时设置 TTL 到当日 24:00，
 * 次日自动归零。Redis 不可用（local 轻量模式）时静默降级，不影响主流程。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DailyStatsCounter {

    private static final String PREFIX = "stats:";
    private static final String SUFFIX = ":daily";

    private final ObjectProvider<RedisUtil> redisUtilProvider;

    /**
     * 自增指定业务域当日的计数器，返回自增后的值；Redis 不可用时返回 null。
     */
    public Long incrementToday(String domain) {
        RedisUtil redisUtil = redisUtilProvider.getIfAvailable();
        if (redisUtil == null) {
            return null;
        }
        try {
            return redisUtil.incrementWithExpire(buildKey(domain, LocalDate.now()), ttlUntilEndOfDay(), TimeUnit.SECONDS);
        } catch (RuntimeException e) {
            log.warn("Failed to increment daily counter for domain {}: {}", domain, e.getMessage());
            return null;
        }
    }

    /**
     * 读取指定业务域当日的计数器值；Redis 不可用或未计数时返回 0。
     */
    public long getTodayCount(String domain) {
        RedisUtil redisUtil = redisUtilProvider.getIfAvailable();
        if (redisUtil == null) {
            return 0L;
        }
        try {
            Long value = redisUtil.getLong(buildKey(domain, LocalDate.now()));
            return value == null ? 0L : value;
        } catch (RuntimeException e) {
            log.warn("Failed to read daily counter for domain {}: {}", domain, e.getMessage());
            return 0L;
        }
    }

    private String buildKey(String domain, LocalDate date) {
        return PREFIX + domain + SUFFIX + ":" + date.toString().replace("-", "");
    }

    private long ttlUntilEndOfDay() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfDay = LocalDateTime.of(now.toLocalDate(), LocalTime.MAX);
        Duration duration = Duration.between(now, endOfDay);
        long seconds = duration.getSeconds();
        // 至少保留 1 秒，避免边界上为 0
        return Math.max(seconds, 1L);
    }
}
