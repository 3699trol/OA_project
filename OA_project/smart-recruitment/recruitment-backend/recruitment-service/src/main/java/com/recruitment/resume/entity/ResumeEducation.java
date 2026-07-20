package com.recruitment.resume.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 教育经历实体
 */
@Data
@TableName("resume_education")
public class ResumeEducation {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long resumeId;
    private String school;
    private String major;
    private String degree;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer isHighest;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
