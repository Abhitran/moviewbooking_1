package com.xyz.booking.offer;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 50% discount on the 3rd ticket (cheapest of the 3rd seat price).
 * Applies when booking 3 or more seats.
 */
@Component
public class ThirdTicketDiscountStrategy implements OfferStrategy {

    @Override
    public DiscountResult apply(BookingContext context) {
        if (context.getSeatCount() < 3) {
            return DiscountResult.noDiscount();
        }
        // 3rd cheapest seat gets 50% off
        BigDecimal thirdSeatPrice = context.getSortedSeatPrices().get(2);
        BigDecimal discount = thirdSeatPrice.multiply(BigDecimal.valueOf(0.5));
        return new DiscountResult(discount, "THIRD_TICKET_50");
    }
}
