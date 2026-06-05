package com.example.cityactivity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CityActivityApplication {
    public static void main(String[] args) {
        SpringApplication.run(CityActivityApplication.class, args);
    }
}
