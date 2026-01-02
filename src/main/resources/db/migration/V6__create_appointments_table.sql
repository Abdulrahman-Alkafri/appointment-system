-- Create appointments table
CREATE TABLE appointments (
                              id BIGSERIAL PRIMARY KEY,

                              customer_id BIGINT NOT NULL,
                              employee_id BIGINT NOT NULL,
                              start_time TIMESTAMP NOT NULL,
                              end_time TIMESTAMP NOT NULL,
                              service_id BIGINT,
                              status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',

                              CONSTRAINT fk_appointments_customer FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE,
                              CONSTRAINT fk_appointments_employee FOREIGN KEY (employee_id) REFERENCES users(id) ON DELETE CASCADE,
                              CONSTRAINT fk_appointments_service FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE SET NULL
);

-- Create indexes for faster lookups
CREATE INDEX idx_appointments_customer_id ON appointments(customer_id);
CREATE INDEX idx_appointments_employee_id ON appointments(employee_id);
CREATE INDEX idx_appointments_service_id ON appointments(service_id);
CREATE INDEX idx_appointments_start_time ON appointments(start_time);
CREATE INDEX idx_appointments_end_time ON appointments(end_time);
CREATE INDEX idx_appointments_status ON appointments(status);

