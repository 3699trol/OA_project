package com.recruitment.common.web.config;

import com.recruitment.common.core.constant.CommonConstant;
import com.recruitment.common.web.interceptor.RequestIdInterceptor;
import com.recruitment.common.web.interceptor.UserContextInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final RequestIdInterceptor requestIdInterceptor;
    private final UserContextInterceptor userContextInterceptor;

    public WebMvcConfig(RequestIdInterceptor requestIdInterceptor,
                        UserContextInterceptor userContextInterceptor) {
        this.requestIdInterceptor = requestIdInterceptor;
        this.userContextInterceptor = userContextInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestIdInterceptor).addPathPatterns("/**");
        registry.addInterceptor(userContextInterceptor).addPathPatterns("/api/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
