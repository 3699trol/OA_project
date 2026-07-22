package com.recruitment.job.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 职位分类实体
 */
@Data
@TableName("job_category")
public class JobCategory {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private Integer status; // 1-启用 0-停用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;

    @TableField(exist = false)
    private Integer jobCount; // 关联职位数（非数据库字段，实时统计）
}
