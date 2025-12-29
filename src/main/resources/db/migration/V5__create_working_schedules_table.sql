-- Create working_schedules table
CREATE TABLE working_schedules (
    id BIGSERIAL PRIMARY KEY,
    day VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL
);

-- Create index on day for faster lookups
CREATE INDEX idx_working_schedules_day ON working_schedules(day);

