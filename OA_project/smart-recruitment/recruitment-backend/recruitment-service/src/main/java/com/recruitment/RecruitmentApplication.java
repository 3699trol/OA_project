package com.recruitment;

import com.recruitment.common.core.config.RequiredConfigCenterInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.recruitment.api.client")
public class RecruitmentApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(RecruitmentApplication.class);
        application.addInitializers(new RequiredConfigCenterInitializer("recruitment-service"));
        application.run(args);
    }
}
