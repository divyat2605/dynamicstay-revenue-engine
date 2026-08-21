package com.dynamicstay.service;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OccupancyEventRequested(
        Long roomId,
        String eventType,
        double occupancyAtEvent,
        LocalDate date,
        String strategyUsed,
        BigDecimal finalPrice) {
}
