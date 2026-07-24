package com.recruitment.validation;

import com.recruitment.auth.dto.ChangePasswordRequest;
import com.recruitment.auth.dto.LoginRequest;
import com.recruitment.auth.dto.RegisterRequest;
import com.recruitment.auth.dto.UpdateProfileRequest;
import com.recruitment.api.dto.AiMockInterviewStartRequest;
import com.recruitment.api.dto.AiQuestionRequest;
import com.recruitment.interview.dto.InterviewCreateRequest;
import com.recruitment.interview.dto.InterviewEvaluationRequest;
import com.recruitment.interview.dto.InterviewQuestionGenerateRequest;
import com.recruitment.job.dto.JobCategoryRequest;
import com.recruitment.job.dto.JobCreateRequest;
import com.recruitment.job.dto.JobUpdateRequest;
import com.recruitment.system.dto.RoleUpdateRequest;
import com.recruitment.system.dto.UserUpdateRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestValidationTest {

    private static ValidatorFactory validatorFactory;
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
    void registerRejectsFrontendBypassAndAdminSelfRegistration() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("abc");
        request.setPassword("12345");
        request.setRealName(" ");
        request.setPhone("123456");
        request.setEmail("invalid-email");
        request.setUserType(1);

        assertEquals(
                Set.of("username", "password", "realName", "phone", "email", "userType"),
                invalidProperties(request));
    }

    @Test
    void loginAndChangePasswordEnforcePasswordLength() {
        LoginRequest login = new LoginRequest();
        login.setUsername("candidate@example.com");
        login.setPassword("12345");
        assertEquals(Set.of("password"), invalidProperties(login));

        ChangePasswordRequest changePassword = new ChangePasswordRequest();
        changePassword.setOldPassword("x".repeat(65));
        changePassword.setNewPassword("12345");
        assertEquals(Set.of("oldPassword", "newPassword"), invalidProperties(changePassword));
    }

    @Test
    void profileAndAdminUserRejectInvalidContactAndStateFields() {
        UpdateProfileRequest profile = new UpdateProfileRequest();
        profile.setRealName(" ");
        profile.setPhone("10086");
        profile.setEmail("invalid-email");
        profile.setAvatarUrl("x".repeat(256));
        assertEquals(
                Set.of("realName", "phone", "email", "avatarUrl"),
                invalidProperties(profile));

        UserUpdateRequest user = new UserUpdateRequest();
        user.setRealName(" ");
        user.setUserType(0);
        user.setPhone("123");
        user.setEmail("invalid-email");
        user.setStatus(2);
        assertEquals(
                Set.of("realName", "userType", "phone", "email", "status"),
                invalidProperties(user));
    }

    @Test
    void roleAndCategoryRequestsEnforceFrontendRules() {
        RoleUpdateRequest role = new RoleUpdateRequest();
        role.setRoleCode("admin");
        role.setRoleName(" ");
        role.setDescription("x".repeat(256));
        role.setStatus(2);
        assertEquals(
                Set.of("roleCode", "roleName", "description", "status"),
                invalidProperties(role));

        RoleUpdateRequest statusOnly = new RoleUpdateRequest();
        statusOnly.setStatus(1);
        assertTrue(invalidProperties(statusOnly).isEmpty());

        JobCategoryRequest category = new JobCategoryRequest();
        category.setName(" ");
        category.setDescription("x".repeat(501));
        category.setStatus(-1);
        assertEquals(Set.of("name", "description", "status"), invalidProperties(category));
    }

    @Test
    void jobRequestsRejectMissingNamesAndInvalidNumericBounds() {
        JobCreateRequest create = new JobCreateRequest();
        create.setJobName(" ");
        create.setCategory("");
        create.setHeadcount(0);
        create.setSalaryMin(new BigDecimal("-1"));
        create.setSalaryMax(new BigDecimal("-2"));
        create.setSkills("x".repeat(501));
        assertEquals(
                Set.of("jobName", "category", "headcount", "salaryMin", "salaryMax", "skills"),
                invalidProperties(create));

        JobUpdateRequest update = new JobUpdateRequest();
        update.setJobName("");
        update.setCategory(" ");
        update.setHeadcount(null);
        assertEquals(Set.of("jobName", "category", "headcount"), invalidProperties(update));
    }

    @Test
    void interviewRequestsRejectInvalidSelectionsAndScores() {
        InterviewCreateRequest create = new InterviewCreateRequest();
        create.setCandidateId(0L);
        create.setApplicationId(null);
        create.setInterviewerId(-1L);
        create.setType("其他");
        create.setMode("电话");
        create.setInterviewTime("2026/07/24");
        create.setAddress("x".repeat(256));
        assertEquals(
                Set.of("candidateId", "applicationId", "interviewerId", "type", "mode",
                        "interviewTime", "address"),
                invalidProperties(create));

        InterviewEvaluationRequest evaluation = new InterviewEvaluationRequest();
        evaluation.setInterviewId(0L);
        evaluation.setTechnicalScore(-1);
        evaluation.setCommunicationScore(101);
        evaluation.setLogicScore(null);
        evaluation.setOverallScore(101);
        evaluation.setResult(3);
        assertEquals(
                Set.of("interviewId", "technicalScore", "communicationScore", "logicScore",
                        "overallScore", "result"),
                invalidProperties(evaluation));
    }

    @Test
    void aiQuestionCountsCannotBypassFrontendSliders() {
        AiMockInterviewStartRequest mockInterview = new AiMockInterviewStartRequest();
        mockInterview.setJobId(0L);
        mockInterview.setJobTitle(" ");
        mockInterview.setType("other");
        mockInterview.setDifficulty("expert");
        mockInterview.setCount(11);
        assertEquals(
                Set.of("jobId", "jobTitle", "type", "difficulty", "count"),
                invalidProperties(mockInterview));

        AiQuestionRequest aiQuestion = new AiQuestionRequest();
        aiQuestion.setJobTitle(" ");
        aiQuestion.setJobDescription("");
        aiQuestion.setCount(21);
        assertEquals(Set.of("jobTitle", "jobDescription", "count"), invalidProperties(aiQuestion));

        InterviewQuestionGenerateRequest formalQuestion = new InterviewQuestionGenerateRequest();
        formalQuestion.setJobId(-1L);
        formalQuestion.setQuestionType("其他");
        formalQuestion.setDifficulty("极难");
        formalQuestion.setCount(0);
        assertEquals(
                Set.of("jobId", "questionType", "difficulty", "count"),
                invalidProperties(formalQuestion));
    }

    private Set<String> invalidProperties(Object request) {
        return validator.validate(request).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }
}
