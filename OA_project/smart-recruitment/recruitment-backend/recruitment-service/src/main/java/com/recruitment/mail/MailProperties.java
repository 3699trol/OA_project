package com.recruitment.mail;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 邮件配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "recruitment.mail")
public class MailProperties {

    /** 发件人邮箱 */
    private String from = "noreply@company.com";

    /** 发件人显示名称 */
    private String fromName = "智能招聘系统";
}
