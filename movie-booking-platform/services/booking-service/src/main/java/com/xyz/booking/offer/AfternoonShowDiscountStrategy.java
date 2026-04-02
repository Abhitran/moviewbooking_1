package com.xyz.booking.offer;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * 20% discount on total amount for shows starting between 12:00 and 17:00.
 */
@Component
public class AfternoonShowDiscountStrategy implements OfferStrategy {

    private static final LocalTime AFTERNOON_START = LocalTime.of(12, 0);
    private static final LocalTime AFTERNOON_END   = LocalTime.of(17, 0);

    @Override
    public DiscountResult apply(BookingContext context) {
        LocalTime showTime = context.getShowTime();
        if (showTime == null) {
            return DiscountResult.noDiscount();
        }
        boolean isAfternoon = !showTime.isBefore(AFTERNOON_START) && showTime.isBefore(AFTERNOON_END);
        if (!isAfternoon) {
            return DiscountResult.noDiscount();
        }
        BigDecimal total = context.getSeatPrices().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = total.multiply(BigDecimal.valueOf(0.2));
        return new DiscountResult(discount, "AFTERNOON_SHOW_20");
    }
}
