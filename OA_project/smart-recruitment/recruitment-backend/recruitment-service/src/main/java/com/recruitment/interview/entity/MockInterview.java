package com.recruitment.interview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模拟面试实体
 */
@Data
@TableName("mock_interview")
public class MockInterview {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long jobId;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
