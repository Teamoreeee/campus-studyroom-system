package com.campus.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CampusAuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusAuthServiceApplication.class, args);
    }
}