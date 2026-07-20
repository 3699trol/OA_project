package com.recruitment.resume.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 简历实体
 */
@Data
@TableName("resume")
public class Resume {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String fileUrl;
    private String parsedContent;
    private BigDecimal aiScore;
    private String aiAnalysis;
    private BigDecimal currentSalary;
    private BigDecimal expectedSalary;
    private String selfEvaluation;
    private String embeddingVector;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
