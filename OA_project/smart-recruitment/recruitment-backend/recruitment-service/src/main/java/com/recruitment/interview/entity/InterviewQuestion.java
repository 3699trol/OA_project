package com.recruitment.interview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 面试题实体
 */
@Data
@TableName("interview_question")
public class InterviewQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long interviewId;
    private String title;
    private String questionType;
    private String difficulty;
    private String referenceAnswer;
    private String scoringCriteria;
    private Integer sortOrder;
    private Boolean isAiGenerated;
    private LocalDateTime createTime;
}
