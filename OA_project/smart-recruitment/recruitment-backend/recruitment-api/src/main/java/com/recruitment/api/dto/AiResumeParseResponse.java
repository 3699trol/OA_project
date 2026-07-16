package com.recruitment.api.dto;

import lombok.Data;
import java.util.List;

/**
 * AI简历解析响应
 */
@Data
public class AiResumeParseResponse {
    private String name;
    private String email;
    private String phone;
    private String education;
    private String school;
    private String major;
    private Integer workYears;
    private List<String> skills;
    private String summary;
    private List<WorkExperience> workExperiences;

    @Data
    public static class WorkExperience {
        private String company;
        private String position;
        private String startDate;
        private String endDate;
        private String description;
    }
}
