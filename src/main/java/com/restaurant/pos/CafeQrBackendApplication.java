package com.restaurant.pos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class CafeQrBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CafeQrBackendApplication.class, args);
    }

}
