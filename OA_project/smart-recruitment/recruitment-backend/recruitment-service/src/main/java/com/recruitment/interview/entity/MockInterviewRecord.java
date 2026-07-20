package com.recruitment.interview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 模拟面试记录实体
 */
@Data
@TableName("mock_interview_record")
public class MockInterviewRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long mockInterviewId;
    private String question;
    private String answer;
    private String followUp;
    private BigDecimal score;
    private LocalDateTime createTime;
}
