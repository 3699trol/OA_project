package com.recruitment.application.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 职位投递实体
 */
@Data
@TableName("job_application")
public class JobApplication {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long jobId;
    private Long userId;
    private Long resumeId;
    private Integer status; // 0-已投递 1-初筛通过 2-初筛不通过 3-面试中 4-录用 5-拒绝
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
