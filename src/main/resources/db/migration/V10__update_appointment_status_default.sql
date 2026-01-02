-- Update appointments table to set default status to PENDING for new appointments
ALTER TABLE appointments ALTER COLUMN status SET DEFAULT 'PENDING';

-- Update any existing appointments that are SCHEDULED but were just created (if needed)
-- This is just for the transition - existing appointments remain as they are