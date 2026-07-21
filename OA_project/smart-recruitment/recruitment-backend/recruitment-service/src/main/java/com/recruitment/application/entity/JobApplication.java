package com.recruitment.application.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import lombok.Data;

import java.math.BigDecimal;
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
    @TableField(insertStrategy = FieldStrategy.ALWAYS)
    private Long resumeId;
    private LocalDateTime applyTime;
    private Integer status; // 0-待筛选 1-面试中 2-录用 3-淘汰 4-撤回
    private String remark;
    private BigDecimal aiMatchScore;
    private String aiMatchReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
