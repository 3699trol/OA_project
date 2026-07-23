package com.recruitment.auth.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Authentication and user-cache settings that can be refreshed from Nacos.
 */
@Data
@Component
@RefreshScope
@Validated
@ConfigurationProperties(prefix = "recruitment.auth")
public class AuthRuntimeProperties {

    @NotNull
    @DurationMin(seconds = 1)
    @DurationMax(days = 7)
    private Duration userCacheTtl = Duration.ofMinutes(30);

    @NotNull
    @DurationMin(seconds = 1)
    @DurationMax(days = 1)
    private Duration loginFailureWindow = Duration.ofMinutes(10);

    @Min(1)
    @Max(100)
    private long maxLoginFailures = 5;
}
