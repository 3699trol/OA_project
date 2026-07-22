package com.recruitment.ai.client;

import java.util.List;
import java.util.Map;

public final class AiResponseSchemas {

    private AiResponseSchemas() {
    }

    public static Map<String, Object> resumeParse() {
        Map<String, Object> educationExperience = object(
                Map.of(
                        "school", nullable("string"),
                        "major", nullable("string"),
                        "degree", nullable("string"),
                        "startDate", nullable("string"),
                        "endDate", nullable("string")
                ),
                List.of("school", "major", "degree", "startDate", "endDate"));

        Map<String, Object> workExperience = object(
                Map.of(
                        "company", nullable("string"),
                        "position", nullable("string"),
                        "startDate", nullable("string"),
                        "endDate", nullable("string"),
                        "description", nullable("string")
                ),
                List.of("company", "position", "startDate", "endDate", "description"));

        return object(
                Map.ofEntries(
                        Map.entry("name", nullable("string")),
                        Map.entry("email", nullable("string")),
                        Map.entry("phone", nullable("string")),
                        Map.entry("education", nullable("string")),
                        Map.entry("school", nullable("string")),
                        Map.entry("major", nullable("string")),
                        Map.entry("educationExperiences", array(educationExperience)),
                        Map.entry("workYears", nullable("integer")),
                        Map.entry("skills", array(Map.of("type", "string"))),
                        Map.entry("summary", nullable("string")),
                        Map.entry("workExperiences", array(workExperience)),
                        Map.entry("overallScore", integer(0, 100)),
                        Map.entry("evaluation", Map.of("type", "string")),
                        Map.entry("strengths", array(Map.of("type", "string"))),
                        Map.entry("issues", array(Map.of("type", "string"))),
                        Map.entry("suggestions", array(Map.of("type", "string"))),
                        Map.entry("optimizedSummary", nullable("string"))
                ),
                List.of("name", "email", "phone", "education", "school", "major", "educationExperiences", "workYears",
                        "skills", "summary", "workExperiences", "overallScore", "evaluation",
                        "strengths", "issues", "suggestions", "optimizedSummary"));
    }

    public static Map<String, Object> match() {
        return object(
                Map.of(
                        "totalScore", number(),
                        "skillMatchScore", number(),
                        "educationMatchScore", number(),
                        "experienceMatchScore", number(),
                        "explanation", Map.of("type", "string"),
                        "strengths", array(Map.of("type", "string")),
                        "weaknesses", array(Map.of("type", "string"))
                ),
                List.of("totalScore", "skillMatchScore", "educationMatchScore", "experienceMatchScore",
                        "explanation", "strengths", "weaknesses"));
    }

    public static Map<String, Object> questions() {
        Map<String, Object> question = object(
                Map.of(
                        "title", Map.of("type", "string"),
                        "type", Map.of("type", "string"),
                        "difficulty", Map.of("type", "string"),
                        "referenceAnswer", Map.of("type", "string"),
                        "scoringCriteria", Map.of("type", "string")
                ),
                List.of("title", "type", "difficulty", "referenceAnswer", "scoringCriteria"));
        return object(Map.of("questions", array(question)), List.of("questions"));
    }

    public static Map<String, Object> mockInterviewStart() {
        return object(
                Map.of("welcomeMessage", Map.of("type", "string")),
                List.of("welcomeMessage"));
    }

    public static Map<String, Object> mockInterviewChat() {
        return object(
                Map.of(
                        "reply", Map.of("type", "string"),
                        "suggestedQuestions", array(Map.of("type", "string"))
                ),
                List.of("reply", "suggestedQuestions"));
    }

    public static Map<String, Object> mockInterviewSubmit() {
        Map<String, Object> scores = object(
                Map.of(
                        "tech", integer(0, 100),
                        "logic", integer(0, 100),
                        "communication", integer(0, 100)
                ),
                List.of("tech", "logic", "communication"));

        Map<String, Object> reportData = object(
                Map.of(
                        "overall", Map.of("type", "string"),
                        "scores", scores,
                        "details", Map.of("type", "string"),
                        "weaknesses", array(Map.of("type", "string"))
                ),
                List.of("overall", "scores", "details", "weaknesses"));

        return object(
                Map.of("report", reportData),
                List.of("report"));
    }

    private static Map<String, Object> object(Map<String, Object> properties, List<String> required) {
        return Map.of(
                "type", "object",
                "properties", properties,
                "required", required,
                "additionalProperties", false);
    }

    private static Map<String, Object> nullable(String type) {
        return Map.of("type", List.of(type, "null"));
    }

    private static Map<String, Object> number() {
        return Map.of("type", "number");
    }

    private static Map<String, Object> integer(int minimum, int maximum) {
        return Map.of("type", "integer", "minimum", minimum, "maximum", maximum);
    }

    private static Map<String, Object> array(Map<String, Object> items) {
        return Map.of("type", "array", "items", items);
    }
}
