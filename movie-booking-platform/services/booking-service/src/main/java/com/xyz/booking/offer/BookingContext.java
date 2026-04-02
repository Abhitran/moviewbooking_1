package com.xyz.booking.offer;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class BookingContext {
    private List<BigDecimal> seatPrices;
    private LocalTime showTime;

    public int getSeatCount() {
        return seatPrices != null ? seatPrices.size() : 0;
    }

    /** Returns seat prices sorted ascending so index 2 = 3rd cheapest seat. */
    public List<BigDecimal> getSortedSeatPrices() {
        return seatPrices.stream().sorted().toList();
    }
}
