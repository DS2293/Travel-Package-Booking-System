-- Insert users data with BCrypt encrypted passwords
-- password123 -> $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.
-- agent123 -> $2a$10$HKUxTkq5jO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.
-- admin123 -> $2a$10$DowJonesIndex5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.

INSERT INTO users (user_id, name, email, password, role, contact_number, approval, registration_date, enabled) VALUES
(1, 'John Smith', 'john.smith@email.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'customer', '+1-555-0101', 'approved', NOW(), true),
(2, 'Sarah Johnson', 'sarah.johnson@email.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'customer', '+1-555-0102', 'approved', NOW(), true),
(3, 'Mike Davis', 'mike.davis@email.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'customer', '+1-555-0103', 'approved', NOW(), true),
(4, 'Emily Wilson', 'emily.wilson@email.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'customer', '+1-555-0104', 'approved', NOW(), true),
(5, 'David Brown', 'david.brown@email.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'customer', '+1-555-0105', 'approved', NOW(), true),
(6, 'Lisa Garcia', 'lisa.garcia@email.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'customer', '+1-555-0106', 'approved', NOW(), true),
(7, 'Agent Alex', 'alex.agent@travel.com', '$2a$10$HKUxTkq5jO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'agent', '+1-555-0201', 'approved', NOW(), true),
(8, 'Agent Maria', 'maria.agent@travel.com', '$2a$10$HKUxTkq5jO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'agent', '+1-555-0202', 'approved', NOW(), true),
(9, 'Agent Tom', 'tom.agent@travel.com', '$2a$10$HKUxTkq5jO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'agent', '+1-555-0203', 'approved', NOW(), true),
(10, 'Admin User', 'admin@travel.com', '$2a$10$DowJonesIndex5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'admin', '+1-555-0301', 'approved', NOW(), true); 