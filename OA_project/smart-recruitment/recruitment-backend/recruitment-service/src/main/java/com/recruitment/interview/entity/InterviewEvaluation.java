package com.recruitment.interview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 面试评价实体
 */
@Data
@TableName("interview_evaluation")
public class InterviewEvaluation {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long interviewId;
    private BigDecimal totalScore;
    private String technicalScore;
    private String communicationScore;
    private String summary;
    private String recommendation; // PASS / REJECT / PENDING
    private Long evaluatorId;
    private LocalDateTime createTime;
}
