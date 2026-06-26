package com.campus.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CampusUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusUserServiceApplication.class, args);
    }
}