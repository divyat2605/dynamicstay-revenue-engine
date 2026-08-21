package com.dynamicstay.pricing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class OccupancyBasedPricingTest {

    private final OccupancyBasedPricing strategy = new OccupancyBasedPricing();

    @ParameterizedTest(name = "occupancy={0} -> multiplier applied to base 100 == {1}")
    @CsvSource({
            "0.95, 150.00",
            "0.90, 150.00",
            "0.75, 125.00",
            "0.70, 125.00",
            "0.50, 105.00",
            "0.40, 105.00",
            "0.20, 85.00",
            "0.00, 85.00"
    })
    void tieredMultiplierAppliesCorrectly(double occupancy, String expected) {
        PricingContext context = contextWithOccupancy(occupancy);

        BigDecimal price = strategy.calculatePrice(context);

        assertThat(price).isEqualByComparingTo(expected);
    }

    @Test
    void reportsCorrectType() {
        assertThat(strategy.getType()).isEqualTo(PricingStrategyType.OCCUPANCY_BASED);
    }

    private PricingContext contextWithOccupancy(double occupancy) {
        return PricingContext.builder()
                .baseRate(new BigDecimal("100.00"))
                .checkInDate(LocalDate.of(2026, 3, 10))
                .checkOutDate(LocalDate.of(2026, 3, 12))
                .currentOccupancyRate(occupancy)
                .today(LocalDate.of(2026, 2, 1))
                .build();
    }
}
