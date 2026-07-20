package com.recruitment.job.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新职位请求
 */
@Data
public class JobUpdateRequest {

    private String jobName;
    private String department;
    private String category;
    private String city;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String education;
    private String experience;
    private Integer headcount;
    private String description;
    private String requirements;
}
