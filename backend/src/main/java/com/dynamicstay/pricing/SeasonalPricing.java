package com.dynamicstay.pricing;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Adjusts price based on calendar season: peak leisure months (summer +
 * December) command a premium; shoulder/off-peak months get a modest
 * discount to stimulate demand.
 */
@Component
public class SeasonalPricing implements PricingStrategy {

    private static final BigDecimal PEAK_MULTIPLIER = new BigDecimal("1.25");
    private static final BigDecimal OFF_PEAK_MULTIPLIER = new BigDecimal("0.90");

    @Override
    public BigDecimal calculatePrice(PricingContext context) {
        BigDecimal multiplier = context.isPeakSeason() ? PEAK_MULTIPLIER : OFF_PEAK_MULTIPLIER;
        return context.getBaseRate()
                .multiply(multiplier)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public PricingStrategyType getType() {
        return PricingStrategyType.SEASONAL;
    }
}
