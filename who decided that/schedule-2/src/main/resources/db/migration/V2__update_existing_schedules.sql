-- Set default values for existing records in the schedule table
UPDATE schedule 
SET 
    start_time = '09:00:00',
    end_time = '10:00:00',
    subject = 'Default Subject',
    day_of_week = 'MONDAY'
WHERE start_time IS NULL OR end_time IS NULL OR subject IS NULL OR day_of_week IS NULL;
