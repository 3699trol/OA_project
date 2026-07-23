package com.recruitment.auth.config;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthRuntimePropertiesTest {

    private static jakarta.validation.ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void acceptsDefaultValues() {
        assertTrue(validator.validate(new AuthRuntimeProperties()).isEmpty());
    }

    @Test
    void rejectsUnsafeLoginLimits() {
        AuthRuntimeProperties properties = new AuthRuntimeProperties();
        properties.setMaxLoginFailures(0);
        properties.setLoginFailureWindow(Duration.ZERO);

        assertFalse(validator.validate(properties).isEmpty());
    }

    @Test
    void rejectsExcessiveCacheDuration() {
        AuthRuntimeProperties properties = new AuthRuntimeProperties();
        properties.setUserCacheTtl(Duration.ofDays(8));

        assertFalse(validator.validate(properties).isEmpty());
    }
}
