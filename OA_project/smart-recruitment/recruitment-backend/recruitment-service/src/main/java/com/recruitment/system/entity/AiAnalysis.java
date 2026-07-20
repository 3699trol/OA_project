package com.recruitment.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI分析记录实体
 */
@Data
@TableName("ai_analysis")
public class AiAnalysis {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String type;
    private String requestData;
    private String responseData;
    private Long userId;
    private LocalDateTime createTime;
}
