-- Booking Service Database Schema

CREATE TABLE IF NOT EXISTS bookings (
    booking_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL,
    show_id         UUID NOT NULL,
    total_amount    DECIMAL(10,2) NOT NULL,
    discount_amount DECIMAL(10,2) DEFAULT 0,
    final_amount    DECIMAL(10,2) NOT NULL,
    status          VARCHAR(50) NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED')),
    offer_applied   VARCHAR(100),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_bookings_user   ON bookings(user_id);
CREATE INDEX idx_bookings_show   ON bookings(show_id);
CREATE INDEX idx_bookings_status ON bookings(status);

CREATE TABLE IF NOT EXISTS booking_seats (
    booking_seat_id  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id       UUID NOT NULL REFERENCES bookings(booking_id) ON DELETE CASCADE,
    seat_number      VARCHAR(10) NOT NULL,
    price            DECIMAL(10,2) NOT NULL,
    discount_applied DECIMAL(10,2) DEFAULT 0
);

CREATE INDEX idx_booking_seats_booking ON booking_seats(booking_id);
