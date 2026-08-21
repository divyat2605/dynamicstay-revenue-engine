package com.dynamicstay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for DynamicStay — a Hotel Revenue & Booking Management System
 * that simulates dynamic pricing logic (Strategy pattern) on top of a
 * PostgreSQL transactional store and a MongoDB analytics/document store.
 */
@SpringBootApplication
public class DynamicStayApplication {

    public static void main(String[] args) {
        SpringApplication.run(DynamicStayApplication.class, args);
    }
}
