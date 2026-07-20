package com.recruitment.interview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

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
    private Integer technicalScore;
    private Integer communicationScore;
    private Integer logicScore;
    private Integer overallScore;
    private String evaluation;
    private Integer result; // 0-淘汰 1-待定 2-通过
    private LocalDateTime feedbackTime;
}
