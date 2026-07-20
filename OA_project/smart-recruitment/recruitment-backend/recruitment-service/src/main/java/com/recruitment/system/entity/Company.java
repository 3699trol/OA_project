package com.recruitment.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公司信息实体
 */
@Data
@TableName("company")
public class Company {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String companyName;
    private String shortName;
    private String industry;
    private String scale;
    private String city;
    private String address;
    private String logoUrl;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
