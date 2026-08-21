package com.dynamicstay.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceQuoteResponse {
    private Long roomId;
    private BigDecimal baseRate;
    private BigDecimal quotedPricePerNight;
    private long nights;
    private BigDecimal totalPrice;
    private String strategyUsed;
    private double occupancyRateAtQuote;
    private long daysUntilCheckIn;
    private BigDecimal seasonalAdjustment;
    private BigDecimal occupancyAdjustment;
    private BigDecimal lastMinuteAdjustment;
}
