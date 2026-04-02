-- Create theatres table
CREATE TABLE theatres (
    theatre_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    partner_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    address TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING_APPROVAL' CHECK (status IN ('PENDING_APPROVAL', 'APPROVED', 'REJECTED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for theatres
CREATE INDEX idx_theatres_city ON theatres(city);
CREATE INDEX idx_theatres_partner ON theatres(partner_id);
CREATE INDEX idx_theatres_status ON theatres(status);

-- Create screens table
CREATE TABLE screens (
    screen_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    theatre_id UUID NOT NULL REFERENCES theatres(theatre_id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    total_seats INT NOT NULL,
    seat_layout JSONB NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_total_seats_positive CHECK (total_seats > 0)
);

-- Create indexes for screens
CREATE INDEX idx_screens_theatre ON screens(theatre_id);

-- Create shows table
CREATE TABLE shows (
    show_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    screen_id UUID NOT NULL REFERENCES screens(screen_id) ON DELETE CASCADE,
    movie_name VARCHAR(255) NOT NULL,
    show_date DATE NOT NULL,
    show_time TIME NOT NULL,
    base_price DECIMAL(10,2) NOT NULL,
    language VARCHAR(50),
    genre VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_base_price_positive CHECK (base_price > 0),
    UNIQUE(screen_id, show_date, show_time)
);

-- Create indexes for shows
CREATE INDEX idx_shows_movie ON shows(movie_name, show_date);
CREATE INDEX idx_shows_screen ON shows(screen_id);
CREATE INDEX idx_shows_date ON shows(show_date);
CREATE INDEX idx_shows_movie_language_genre ON shows(movie_name, language, genre);

-- Create seats table
CREATE TABLE seats (
    seat_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    show_id UUID NOT NULL REFERENCES shows(show_id) ON DELETE CASCADE,
    seat_number VARCHAR(10) NOT NULL,
    status VARCHAR(50) DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'BLOCKED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(show_id, seat_number)
);

-- Create indexes for seats
CREATE INDEX idx_seats_show ON seats(show_id);
CREATE INDEX idx_seats_status ON seats(show_id, status);
