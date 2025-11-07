package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableJpaRepositories
@EnableTransactionManagement
public class DemoscheduleApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoscheduleApplication.class, args);
    }
}

// .\gradlew bootRun 05 11 2025 03 02 все успешно забилжено ./gradlew clean bootRun