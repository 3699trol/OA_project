package com.recruitment.job.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 职位实体
 */
@Data
@TableName("job")
public class Job {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String description;
    private String requirements;
    private String category;
    private String location;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private Integer experienceYears;
    private String education;
    private Integer headcount;
    private Integer status; // 0-草稿 1-已发布 2-已下架
    private Long hrUserId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
