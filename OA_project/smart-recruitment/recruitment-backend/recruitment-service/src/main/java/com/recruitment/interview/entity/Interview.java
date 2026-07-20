package com.recruitment.interview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 面试实体
 */
@Data
@TableName("interview")
public class Interview {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long applicationId;
    private Long interviewerId;
    private LocalDateTime interviewTime;
    private String interviewType;
    private String address;
    private String aiQuestions;
    private Integer status; // 0-待面试 1-已完成 2-取消
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
