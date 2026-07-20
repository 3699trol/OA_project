package com.recruitment.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 */
@Data
@TableName("operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String module;
    private String description;
    private Long operatorId;
    private String operatorName;
    private String requestUrl;
    private String requestMethod;
    private String ip;
    private Integer costTime;
    private LocalDateTime createTime;
}
