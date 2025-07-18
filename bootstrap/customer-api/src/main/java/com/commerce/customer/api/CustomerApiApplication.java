package com.commerce.customer.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {
    "com.commerce.customer.core",
    "com.commerce.customer.api",
    "com.commerce.infrastructure.persistence"
})
@EnableDiscoveryClient
public class CustomerApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(CustomerApiApplication.class, args);
    }
}