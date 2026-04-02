package com.xyz.booking.offer;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class DiscountResult {
    BigDecimal discountAmount;
    String offerCode;

    public static DiscountResult noDiscount() {
        return new DiscountResult(BigDecimal.ZERO, null);
    }

    public boolean hasDiscount() {
        return discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) > 0;
    }
}
