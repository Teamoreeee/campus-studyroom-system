package com.campus.room;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CampusRoomServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusRoomServiceApplication.class, args);
    }
}