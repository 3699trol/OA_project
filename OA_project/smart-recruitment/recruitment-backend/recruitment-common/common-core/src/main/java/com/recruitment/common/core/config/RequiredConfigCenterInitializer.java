package com.recruitment.common.core.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

/**
 * Fails dev startup early when required Nacos Data IDs were not loaded.
 */
public final class RequiredConfigCenterInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

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

        boolean commonLoaded = environment.getProperty(COMMON_MARKER, Boolean.class, false);
        String loadedService = environment.getProperty(SERVICE_MARKER, "");
        if (!commonLoaded || !expectedServiceName.equals(loadedService)) {
            throw new IllegalStateException(
                    "Required Nacos configuration is missing for " + expectedServiceName
                            + ". Check recruitment-common.yml and " + expectedServiceName
                            + ".yml, including group and namespace settings.");
        }
    }
}
