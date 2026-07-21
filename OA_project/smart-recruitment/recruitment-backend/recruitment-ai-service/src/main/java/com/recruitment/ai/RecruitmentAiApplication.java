package com.recruitment.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.recruitment.ai",
        "com.recruitment.common.security",
        "com.recruitment.common.web"
})
public class RecruitmentAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecruitmentAiApplication.class, args);
    }
}
