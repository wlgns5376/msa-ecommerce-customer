package com.commerce.infrastructure.persistence;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.commerce.infrastructure.persistence.customer.entity")
@EnableJpaRepositories(basePackages = "com.commerce.infrastructure.persistence.customer.repository")
public class PersistenceTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(PersistenceTestApplication.class, args);
    }
}