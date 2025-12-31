-- Create services table
CREATE TABLE services (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    description TEXT,
    cost INTEGER NOT NULL CHECK (cost >= 0),
    duration INTEGER NOT NULL
);

-- Create index on name for faster lookups
CREATE INDEX idx_services_name ON services(name);

