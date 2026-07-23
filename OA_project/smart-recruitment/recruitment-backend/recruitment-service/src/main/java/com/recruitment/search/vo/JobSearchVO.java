package com.recruitment.search.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class JobSearchVO {
    private Long id;
    private String jobName;
    private String department;
    private String category;
    private String city;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String education;
    private String experience;
    private String description;
    private String requirements;
    private String skills;
    private Integer status;
    private Long publisherId;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
    private Map<String, List<String>> highlights;
}
