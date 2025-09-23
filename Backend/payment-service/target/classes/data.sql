-- Insert payments data
INSERT INTO payments (payment_id, user_id, booking_id, amount, status, payment_method) VALUES
(1, 1, 1, 1299.99, 'completed', 'credit_card'),
(2, 2, 2, 1899.99, 'pending', 'paypal'),
(3, 3, 3, 999.99, 'completed', 'credit_card'),
(4, 4, 4, 1299.99, 'pending', 'paypal'),
(5, 5, 5, 1899.99, 'completed', 'credit_card'),
(6, 6, 6, 999.99, 'pending', 'paypal'),
(7, 1, 7, 1100.00, 'pending', 'credit_card'),
(8, 2, 8, 2200.00, 'completed', 'paypal'),
(9, 3, 9, 1800.00, 'pending', 'credit_card'); 