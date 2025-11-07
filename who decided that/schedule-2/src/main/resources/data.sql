-- Clear existing admin user if it exists
DELETE FROM users WHERE user_id = 'admin' OR email = 'admin@example.com';

-- Insert admin user with hashed password (password is 'admin123')
INSERT INTO users (user_id, full_name, email, password, role, group_id, created_at) 
VALUES (
    'admin', 
    'Admin User', 
    'admin@example.com', 
    '$2a$10$XptfskLsT1SL/bOzZLzJLe1rhvQ4SHz1g8clfI99YUpFnaMPqshjy', -- bcrypt hash of 'admin123'
    'ADMIN', 
    'admin-group',
    NOW()
);

-- Verify the user was inserted
SELECT * FROM users WHERE user_id = 'admin' OR email = 'admin@example.com';
