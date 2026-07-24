package com.recruitment.job.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
    private String jobName;
    private String department;
    private String category;
    private String city;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String education;
    private String experience;
    private Integer headcount;
    private String description;
    private String requirements;
    private String skills; // 技能标签，逗号分隔
    private Integer status; // 0-草稿 1-招聘中 2-下架
    private Long publisherId;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;

    /** 发布者（HR）姓名：非数据库字段，由服务层关联 sys_user 填充 */
    @TableField(exist = false)
    private String publisherName;
}
