package com.dynamicstay.pricing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class LastMinutePricingTest {

    private final LastMinutePricing strategy = new LastMinutePricing();
    private static final LocalDate TODAY = LocalDate.of(2026, 8, 1);

    @ParameterizedTest(name = "{0} days out -> price == {1}")
    @CsvSource({
            "0, 70.00",
            "1, 70.00",
            "2, 85.00",
            "3, 85.00",
            "5, 95.00",
            "7, 95.00",
            "8, 100.00",
            "30, 100.00"
    })
    void discountsScaleWithProximityToCheckIn(int daysOut, String expected) {
        PricingContext context = PricingContext.builder()
                .baseRate(new BigDecimal("100.00"))
                .checkInDate(TODAY.plusDays(daysOut))
                .checkOutDate(TODAY.plusDays(daysOut + 2))
                .currentOccupancyRate(0.3)
                .today(TODAY)
                .build();

        BigDecimal price = strategy.calculatePrice(context);

        assertThat(price).isEqualByComparingTo(expected);
    }

    @Test
    void reportsCorrectType() {
        assertThat(strategy.getType()).isEqualTo(PricingStrategyType.LAST_MINUTE);
    }
}
