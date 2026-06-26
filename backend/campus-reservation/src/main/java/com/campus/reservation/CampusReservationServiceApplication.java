package com.campus.reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class CampusReservationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampusReservationServiceApplication.class, args);
    }
}