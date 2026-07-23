package com.recruitment.common.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

/**
 * Warns on dev startup when Nacos config Data IDs were not loaded,
 * but does not prevent the application from starting.
 */
public final class RequiredConfigCenterInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger log = LoggerFactory.getLogger(RequiredConfigCenterInitializer.class);

    private static final String COMMON_MARKER = "recruitment.config-center.common-loaded";
    private static final String SERVICE_MARKER = "recruitment.config-center.service-loaded";

    private final String expectedServiceName;

    public RequiredConfigCenterInitializer(String expectedServiceName) {
        this.expectedServiceName = expectedServiceName;
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Environment environment = applicationContext.getEnvironment();
        if (!environment.acceptsProfiles(Profiles.of("dev"))) {
            return;
        }
        // 当 Nacos 配置中心被禁用时跳过
        boolean nacosConfigEnabled = environment.getProperty(
                "spring.cloud.nacos.config.enabled", Boolean.class, false);
        if (!nacosConfigEnabled) {
            return;
        }

        boolean commonLoaded = environment.getProperty(COMMON_MARKER, Boolean.class, false);
        String loadedService = environment.getProperty(SERVICE_MARKER, "");
        if (!commonLoaded || !expectedServiceName.equals(loadedService)) {
            log.warn(
                    "Nacos configuration not fully loaded for {} (commonLoaded={}, serviceLoaded={}). "
                            + "Falling back to local application-{}.yml. "
                            + "To manage config via Nacos, publish recruitment-common.yml and {}.yml "
                            + "to group SMART_RECRUITMENT.",
                    expectedServiceName, commonLoaded, loadedService,
                    environment.getActiveProfiles().length > 0
                            ? environment.getActiveProfiles()[0] : "dev",
                    expectedServiceName);
        }
    }
}
