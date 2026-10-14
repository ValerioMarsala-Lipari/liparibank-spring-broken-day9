package com.lipari.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class LipariBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(LipariBankApplication.class, args);
    }
}
