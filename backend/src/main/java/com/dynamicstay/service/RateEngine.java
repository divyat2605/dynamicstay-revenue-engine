package com.dynamicstay.service;

import com.dynamicstay.pricing.LastMinutePricing;
import com.dynamicstay.pricing.OccupancyBasedPricing;
import com.dynamicstay.pricing.PricingContext;
import com.dynamicstay.pricing.PricingStrategy;
import com.dynamicstay.pricing.PricingStrategyType;
import com.dynamicstay.pricing.SeasonalPricing;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Orchestrates the {@link PricingStrategy} implementations. This is the
 * "brain" of DynamicStay: rather than hard-picking a single strategy, it
 * decides which signal should dominate for a given request, then blends
 * strategies so no single lever swings price too violently.
 *
 * <p>Decision rule (deliberately simple / explainable for a portfolio demo):
 * <ul>
 *   <li>If check-in is within 3 days, last-minute urgency dominates (60%),
 *       with occupancy and seasonality each contributing 20%.</li>
 *   <li>Else if current occupancy is at/above 70%, occupancy pressure
 *       dominates (60%), seasonality contributes 40%.</li>
 *   <li>Otherwise, seasonality alone drives price.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class RateEngine {

    private final SeasonalPricing seasonalPricing;
    private final OccupancyBasedPricing occupancyBasedPricing;
    private final LastMinutePricing lastMinutePricing;

    @Value("${pricing.engine.last-minute-threshold-days:3}")
    private int lastMinuteThresholdDays = 3;

    @Value("${pricing.engine.high-occupancy-threshold:0.70}")
    private double highOccupancyThreshold = 0.70;

    public record Quote(
            BigDecimal price,
            PricingStrategyType dominantStrategy,
            BigDecimal seasonalAdjustment,
            BigDecimal occupancyAdjustment,
            BigDecimal lastMinuteAdjustment) {
    }

    public Quote quote(PricingContext context) {
        Objects.requireNonNull(context, "pricing context is required");
        context.validate();

        BigDecimal baseRate = context.getBaseRate().setScale(2, RoundingMode.HALF_UP);
        BigDecimal seasonal = seasonalPricing.calculatePrice(context);

        boolean isLastMinute = context.daysUntilCheckIn() <= lastMinuteThresholdDays;
        boolean isHighOccupancy = context.getCurrentOccupancyRate() >= highOccupancyThreshold;

        BigDecimal blended;
        PricingStrategyType dominant;
        BigDecimal occupancyAdjustment = BigDecimal.ZERO.setScale(2);
        BigDecimal lastMinuteAdjustment = BigDecimal.ZERO.setScale(2);

        if (isLastMinute) {
            BigDecimal occupancy = occupancyBasedPricing.calculatePrice(context);
            BigDecimal lastMinute = lastMinutePricing.calculatePrice(context);
            blended = weighted(lastMinute, "0.60")
                    .add(weighted(occupancy, "0.20"))
                    .add(weighted(seasonal, "0.20"));
            dominant = PricingStrategyType.LAST_MINUTE;
            occupancyAdjustment = adjustment(occupancy, baseRate);
            lastMinuteAdjustment = adjustment(lastMinute, baseRate);
        } else if (isHighOccupancy) {
            BigDecimal occupancy = occupancyBasedPricing.calculatePrice(context);
            blended = weighted(occupancy, "0.60")
                    .add(weighted(seasonal, "0.40"));
            dominant = PricingStrategyType.OCCUPANCY_BASED;
            occupancyAdjustment = adjustment(occupancy, baseRate);
        } else {
            blended = seasonal;
            dominant = PricingStrategyType.SEASONAL;
        }

        return new Quote(
                blended.setScale(2, RoundingMode.HALF_UP),
                dominant,
                adjustment(seasonal, baseRate),
                occupancyAdjustment,
                lastMinuteAdjustment);
    }

    private BigDecimal weighted(BigDecimal price, String weight) {
        return price.multiply(new BigDecimal(weight));
    }

    private BigDecimal adjustment(BigDecimal strategyPrice, BigDecimal baseRate) {
        return strategyPrice.subtract(baseRate).setScale(2, RoundingMode.HALF_UP);
    }
}
