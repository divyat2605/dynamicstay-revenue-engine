package com.dynamicstay.pricing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class SeasonalPricingTest {

    private final SeasonalPricing strategy = new SeasonalPricing();

    @Test
    @DisplayName("Applies a 25% premium in peak months (Jun/Jul/Aug/Dec)")
    void appliesPeakPremium() {
        PricingContext context = PricingContext.builder()
                .baseRate(new BigDecimal("100.00"))
                .checkInDate(LocalDate.of(2026, 7, 15))
                .checkOutDate(LocalDate.of(2026, 7, 17))
                .currentOccupancyRate(0.3)
                .today(LocalDate.of(2026, 6, 1))
                .build();

        BigDecimal price = strategy.calculatePrice(context);

        assertThat(price).isEqualByComparingTo("125.00");
    }

    @Test
    @DisplayName("Applies a 10% discount in off-peak months")
    void appliesOffPeakDiscount() {
        PricingContext context = PricingContext.builder()
                .baseRate(new BigDecimal("100.00"))
                .checkInDate(LocalDate.of(2026, 3, 10))
                .checkOutDate(LocalDate.of(2026, 3, 12))
                .currentOccupancyRate(0.3)
                .today(LocalDate.of(2026, 2, 1))
                .build();

        BigDecimal price = strategy.calculatePrice(context);

        assertThat(price).isEqualByComparingTo("90.00");
    }

    @Test
    @DisplayName("December is treated as peak season")
    void decemberIsPeak() {
        PricingContext context = PricingContext.builder()
                .baseRate(new BigDecimal("200.00"))
                .checkInDate(LocalDate.of(2026, 12, 24))
                .checkOutDate(LocalDate.of(2026, 12, 26))
                .currentOccupancyRate(0.5)
                .today(LocalDate.of(2026, 11, 1))
                .build();

        assertThat(strategy.calculatePrice(context)).isEqualByComparingTo("250.00");
    }

    @Test
    void reportsCorrectType() {
        assertThat(strategy.getType()).isEqualTo(PricingStrategyType.SEASONAL);
    }
}
