-- Create emp_serv join table for many-to-many relationship between users and services
CREATE TABLE emp_serv (
    user_id BIGINT NOT NULL,
    service_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, service_id),
    CONSTRAINT fk_emp_serv_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_emp_serv_service FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE
);

-- Create indexes for faster lookups
CREATE INDEX idx_emp_serv_user_id ON emp_serv(user_id);
CREATE INDEX idx_emp_serv_service_id ON emp_serv(service_id);

