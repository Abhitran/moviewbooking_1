-- Create databases for each service
CREATE DATABASE auth_db;
CREATE DATABASE theatre_db;
CREATE DATABASE booking_db;
CREATE DATABASE payment_db;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE auth_db TO moviebooking;
GRANT ALL PRIVILEGES ON DATABASE theatre_db TO moviebooking;
GRANT ALL PRIVILEGES ON DATABASE booking_db TO moviebooking;
GRANT ALL PRIVILEGES ON DATABASE payment_db TO moviebooking;
