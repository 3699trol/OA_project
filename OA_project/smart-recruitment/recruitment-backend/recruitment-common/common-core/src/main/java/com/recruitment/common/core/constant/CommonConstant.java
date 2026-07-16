package com.recruitment.common.core.constant;

/**
 * 系统常量
 */
public class CommonConstant {

    /** 请求ID Header */
    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    /** Token Header */
    public static final String TOKEN_HEADER = "Authorization";

    /** Token前缀 */
    public static final String TOKEN_PREFIX = "Bearer ";

    /** 默认日期格式 */
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /** 角色常量 */
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_HR = "HR";
    public static final String ROLE_INTERVIEWER = "INTERVIEWER";
    public static final String ROLE_CANDIDATE = "CANDIDATE";
}
