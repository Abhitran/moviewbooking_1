package com.xyz.booking.offer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Evaluates all offer strategies and returns the best (highest) discount.
 * Offers are mutually exclusive — only the highest discount wins.
 */
@Component
@RequiredArgsConstructor
public class OfferEngine {

    private final List<OfferStrategy> strategies;

    public DiscountResult calculateBestOffer(BookingContext context) {
        return strategies.stream()
            .map(s -> s.apply(context))
            .filter(DiscountResult::hasDiscount)
            .max((a, b) -> a.getDiscountAmount().compareTo(b.getDiscountAmount()))
            .orElse(DiscountResult.noDiscount());
    }
}
