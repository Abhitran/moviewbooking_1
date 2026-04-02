package com.xyz.booking.offer;

public interface OfferStrategy {
    DiscountResult apply(BookingContext context);
}
