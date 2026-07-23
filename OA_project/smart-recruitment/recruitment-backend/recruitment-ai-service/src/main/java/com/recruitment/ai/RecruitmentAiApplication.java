package com.recruitment.ai;

import com.recruitment.common.core.config.RequiredConfigCenterInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {
        "com.recruitment.ai",
        "com.recruitment.common.redis",
        "com.recruitment.common.security",
        "com.recruitment.common.web"
})
@EnableDiscoveryClient
public class RecruitmentAiApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(RecruitmentAiApplication.class);
        application.addInitializers(new RequiredConfigCenterInitializer("recruitment-ai-service"));
        application.run(args);
    }
}
