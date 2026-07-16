package com.recruitment.common.core.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 业务错误码 1001-1999
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_DISABLED(1002, "用户已被禁用"),
    USERNAME_EXISTS(1003, "用户名已存在"),
    PASSWORD_ERROR(1004, "密码错误"),
    TOKEN_EXPIRED(1005, "Token已过期"),
    TOKEN_INVALID(1006, "Token无效"),

    // 职位相关 2001-2999
    JOB_NOT_FOUND(2001, "职位不存在"),
    JOB_NOT_PUBLISHED(2002, "职位未发布"),

    // 简历相关 3001-3999
    RESUME_NOT_FOUND(3001, "简历不存在"),
    RESUME_PARSE_ERROR(3002, "简历解析失败"),

    // 投递相关 4001-4999
    APPLICATION_EXISTS(4001, "已投递过该职位"),
    APPLICATION_NOT_FOUND(4002, "投递记录不存在"),

    // 面试相关 5001-5999
    INTERVIEW_NOT_FOUND(5001, "面试记录不存在"),
    QUESTION_GENERATE_ERROR(5002, "面试题生成失败");

    private final int code;
    private final String message;
}
