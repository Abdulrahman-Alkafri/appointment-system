-- Fix duration column type to match Hibernate's expectation (NUMERIC instead of BIGINT)
ALTER TABLE services ALTER COLUMN duration TYPE NUMERIC(21,0) USING duration::numeric;

