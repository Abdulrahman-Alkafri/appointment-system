-- Create emp_work join table for many-to-many relationship between users and working_schedules
CREATE TABLE emp_work (
    user_id BIGINT NOT NULL,
    work_time_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, work_time_id),
    CONSTRAINT fk_emp_work_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_emp_work_work_time FOREIGN KEY (work_time_id) REFERENCES working_schedules(id) ON DELETE CASCADE
);

-- Create indexes for faster lookups
CREATE INDEX idx_emp_work_user_id ON emp_work(user_id);
CREATE INDEX idx_emp_work_work_time_id ON emp_work(work_time_id);

