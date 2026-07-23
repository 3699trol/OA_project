package com.recruitment.config;

import com.recruitment.common.core.config.RequiredConfigCenterInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RequiredConfigCenterInitializerTest {

    private final RequiredConfigCenterInitializer initializer =
            new RequiredConfigCenterInitializer("recruitment-service");

    @Test
    void skipsValidationOutsideDev() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("local");
        GenericApplicationContext context = contextWith(environment);

        assertDoesNotThrow(() -> initializer.initialize(context));
    }

    @Test
    void acceptsMatchingNacosMarkers() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("recruitment.config-center.common-loaded", "true")
                .withProperty("recruitment.config-center.service-loaded", "recruitment-service");
        environment.setActiveProfiles("dev");

        assertDoesNotThrow(() -> initializer.initialize(contextWith(environment)));
    }

    @Test
    void rejectsMissingOrWrongNacosMarkers() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("recruitment.config-center.common-loaded", "true")
                .withProperty("recruitment.config-center.service-loaded", "recruitment-ai-service");
        environment.setActiveProfiles("dev");

        assertThrows(IllegalStateException.class,
                () -> initializer.initialize(contextWith(environment)));
    }

    private GenericApplicationContext contextWith(MockEnvironment environment) {
        GenericApplicationContext context = new GenericApplicationContext();
        context.setEnvironment(environment);
        return context;
    }
}
