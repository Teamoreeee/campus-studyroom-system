package com.campus.attendance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class CampusAttendanceServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampusAttendanceServiceApplication.class, args);
    }
}