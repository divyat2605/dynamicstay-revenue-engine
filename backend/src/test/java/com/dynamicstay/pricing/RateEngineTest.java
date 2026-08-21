package com.dynamicstay.pricing;

import com.dynamicstay.service.RateEngine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies RateEngine's strategy-selection/blending logic in isolation by
 * mocking the three PricingStrategy implementations — this is the class
 * that decides WHICH strategy dominates, so it's tested independently of
 * their actual math (which is covered in the sibling *PricingTest classes).
 */
@ExtendWith(MockitoExtension.class)
class RateEngineTest {

    @Mock
    private SeasonalPricing seasonalPricing;
    @Mock
    private OccupancyBasedPricing occupancyBasedPricing;
    @Mock
    private LastMinutePricing lastMinutePricing;

    @InjectMocks
    private RateEngine rateEngine;

    @Test
    void lastMinuteDominatesWhenCheckInIsWithinThreeDays() {
        when(seasonalPricing.calculatePrice(any())).thenReturn(new BigDecimal("100.00"));
        when(occupancyBasedPricing.calculatePrice(any())).thenReturn(new BigDecimal("100.00"));
        when(lastMinutePricing.calculatePrice(any())).thenReturn(new BigDecimal("70.00"));

        PricingContext context = contextDaysOut(1, 0.3);

        RateEngine.Quote quote = rateEngine.quote(context);

        assertThat(quote.dominantStrategy()).isEqualTo(PricingStrategyType.LAST_MINUTE);
        // 70*0.60 + 100*0.20 + 100*0.20 = 42 + 20 + 20 = 82.00
        assertThat(quote.price()).isEqualByComparingTo("82.00");
        assertThat(quote.seasonalAdjustment()).isEqualByComparingTo("0.00");
        assertThat(quote.occupancyAdjustment()).isEqualByComparingTo("0.00");
        assertThat(quote.lastMinuteAdjustment()).isEqualByComparingTo("-30.00");
    }

    @Test
    void occupancyDominatesWhenHotelIsBusyAndBookingIsNotLastMinute() {
        when(seasonalPricing.calculatePrice(any())).thenReturn(new BigDecimal("100.00"));
        when(occupancyBasedPricing.calculatePrice(any())).thenReturn(new BigDecimal("150.00"));
        when(lastMinutePricing.calculatePrice(any())).thenReturn(new BigDecimal("100.00"));

        PricingContext context = contextDaysOut(10, 0.85);

        RateEngine.Quote quote = rateEngine.quote(context);

        assertThat(quote.dominantStrategy()).isEqualTo(PricingStrategyType.OCCUPANCY_BASED);
        // 150*0.60 + 100*0.40 = 90 + 40 = 130.00
        assertThat(quote.price()).isEqualByComparingTo("130.00");
    }

    @Test
    void seasonalIsTheDefaultWhenNoOtherSignalDominates() {
        when(seasonalPricing.calculatePrice(any())).thenReturn(new BigDecimal("115.00"));

        PricingContext context = contextDaysOut(20, 0.3);

        RateEngine.Quote quote = rateEngine.quote(context);

        assertThat(quote.dominantStrategy()).isEqualTo(PricingStrategyType.SEASONAL);
        assertThat(quote.price()).isEqualByComparingTo("115.00");
        verify(occupancyBasedPricing, never()).calculatePrice(any());
        verify(lastMinutePricing, never()).calculatePrice(any());
    }

    @Test
    void rejectsInvalidDateRangesBeforeInvokingStrategies() {
        PricingContext context = PricingContext.builder()
                .baseRate(new BigDecimal("100.00"))
                .checkInDate(LocalDate.of(2026, 3, 10))
                .checkOutDate(LocalDate.of(2026, 3, 10))
                .currentOccupancyRate(0.3)
                .today(LocalDate.of(2026, 3, 1))
                .build();

        assertThatThrownBy(() -> rateEngine.quote(context))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("checkOut must be after checkIn");
        verify(seasonalPricing, never()).calculatePrice(any());
    }

    private PricingContext contextDaysOut(int daysOut, double occupancy) {
        LocalDate today = LocalDate.of(2026, 3, 1);
        return PricingContext.builder()
                .baseRate(new BigDecimal("100.00"))
                .checkInDate(today.plusDays(daysOut))
                .checkOutDate(today.plusDays(daysOut + 2))
                .currentOccupancyRate(occupancy)
                .today(today)
                .build();
    }
}
