package com.recruitment;

import com.recruitment.common.core.config.RequiredConfigCenterInitializer;
import com.recruitment.mail.MailProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableDiscoveryClient
@EnableAsync
@EnableFeignClients(basePackages = "com.recruitment.api.client")
@EnableConfigurationProperties(MailProperties.class)
public class RecruitmentApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(RecruitmentApplication.class);
        application.addInitializers(new RequiredConfigCenterInitializer("recruitment-service"));
        application.run(args);
    }
}
