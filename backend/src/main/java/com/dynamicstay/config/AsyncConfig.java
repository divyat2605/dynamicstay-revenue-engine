package com.dynamicstay.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Enables @Async so MongoDB occupancy-event logging never blocks the
 * Postgres booking transaction it's triggered from.
 */
@Configuration
@EnableAsync
@EnableRetry
public class AsyncConfig {
}
