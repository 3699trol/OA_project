package com.recruitment.resume.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 简历实体
 */
@Data
@TableName("resume")
public class Resume {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String education;
    private String school;
    private String major;
    private Integer workYears;
    private String skills;
    private String summary;
    private String workExperience;
    private String filePath;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
