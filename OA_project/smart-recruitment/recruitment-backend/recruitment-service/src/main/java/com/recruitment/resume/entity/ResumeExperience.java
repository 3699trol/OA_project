package com.recruitment.resume.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 工作经历实体
 */
@Data
@TableName("resume_experience")
public class ResumeExperience {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long resumeId;
    private String company;
    private String position;
    private String department;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
