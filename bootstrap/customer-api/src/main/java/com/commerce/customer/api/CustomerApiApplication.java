package com.commerce.customer.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "com.commerce.customer.core",
    "com.commerce.customer.api",
    "com.commerce.infrastructure.persistence",
    "com.commerce.infrastructure.kafka"
})
public class CustomerApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(CustomerApiApplication.class, args);
    }
}