package com.recruitment.application.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 投递状态流转日志
 */
@Data
@TableName("application_log")
public class ApplicationLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long applicationId;
    private Integer fromStatus;
    private Integer toStatus;
    private String remark;
    private Long operatorId;
    private LocalDateTime createTime;
}
