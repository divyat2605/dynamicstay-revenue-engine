package com.dynamicstay.pricing;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Adjusts price based on calendar season: peak leisure months (summer +
 * December) command a premium; shoulder/off-peak months get a modest
 * discount to stimulate demand.
 */
@Component
public class SeasonalPricing implements PricingStrategy {

    private final BigDecimal peakMultiplier;
    private final BigDecimal offPeakMultiplier;

    public SeasonalPricing() {
        this(new BigDecimal("1.25"), new BigDecimal("0.90"));
    }

    @Autowired
    public SeasonalPricing(
            @Value("${pricing.seasonal.peak-multiplier:1.25}") BigDecimal peakMultiplier,
            @Value("${pricing.seasonal.off-peak-multiplier:0.90}") BigDecimal offPeakMultiplier) {
        this.peakMultiplier = requirePositive(peakMultiplier, "peakMultiplier");
        this.offPeakMultiplier = requirePositive(offPeakMultiplier, "offPeakMultiplier");
    }

    @Override
    public BigDecimal calculatePrice(PricingContext context) {
        BigDecimal multiplier = context.isPeakSeason() ? peakMultiplier : offPeakMultiplier;
        return context.getBaseRate()
                .multiply(multiplier)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public PricingStrategyType getType() {
        return PricingStrategyType.SEASONAL;
    }

    private BigDecimal requirePositive(BigDecimal value, String name) {
        if (value == null || value.signum() <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return value;
    }
}
