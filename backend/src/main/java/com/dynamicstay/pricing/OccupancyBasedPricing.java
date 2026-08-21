package com.dynamicstay.pricing;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Classic revenue-management demand curve: the fuller the hotel, the higher
 * the marginal room's price. Uses a tiered step function rather than a
 * smooth curve, mirroring how real RMS "rate tiers" are usually configured
 * so the behaviour is easy to explain and test.
 */
@Component
public class OccupancyBasedPricing implements PricingStrategy {

    @Override
    public BigDecimal calculatePrice(PricingContext context) {
        double occupancy = context.getCurrentOccupancyRate();
        BigDecimal multiplier;

        if (occupancy >= 0.90) {
            multiplier = new BigDecimal("1.50");
        } else if (occupancy >= 0.70) {
            multiplier = new BigDecimal("1.25");
        } else if (occupancy >= 0.40) {
            multiplier = new BigDecimal("1.05");
        } else {
            multiplier = new BigDecimal("0.85");
        }

        return context.getBaseRate()
                .multiply(multiplier)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public PricingStrategyType getType() {
        return PricingStrategyType.OCCUPANCY_BASED;
    }
}
