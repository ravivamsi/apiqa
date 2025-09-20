package com.apiqa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ApiqaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiqaApplication.class, args);
    }
}
