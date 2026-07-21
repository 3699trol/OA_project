package com.recruitment.ai.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignAuthorizationConfig {

    @Bean
    public RequestInterceptor authorizationHeaderRelay() {
        return template -> {
            if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
                return;
            }
            HttpServletRequest request = attributes.getRequest();
            String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (StringUtils.hasText(authorization)) {
                template.header(HttpHeaders.AUTHORIZATION, authorization);
            }
        };
    }
}
