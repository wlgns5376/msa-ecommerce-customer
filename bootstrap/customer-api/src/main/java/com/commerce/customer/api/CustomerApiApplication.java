package com.commerce.customer.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
    "com.commerce.customer.core",
    "com.commerce.customer.api",
    "com.commerce.infrastructure.persistence"
})
@EnableJpaRepositories(basePackages = "com.commerce.infrastructure.persistence")
@EntityScan(basePackages = "com.commerce.infrastructure.persistence")
@EnableDiscoveryClient
public class CustomerApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(CustomerApiApplication.class, args);
    }
}