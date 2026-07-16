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
    private Long jobId;
    private Long candidateId;
    private Long interviewerId;
    private Integer status; // 0-待面试 1-面试中 2-已完成 3-已取消
    private LocalDateTime interviewTime;
    private String location;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
