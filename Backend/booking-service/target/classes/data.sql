-- Insert bookings data
INSERT INTO bookings (booking_id, user_id, package_id, start_date, end_date, status, payment_id) VALUES
(1, 1, 1, '2024-03-15', '2024-03-20', 'confirmed', 1),
(2, 2, 2, '2024-04-10', '2024-04-17', 'pending', 2),
(3, 3, 3, '2024-05-05', '2024-05-09', 'confirmed', 3),
(4, 4, 1, '2024-06-12', '2024-06-17', 'pending', 4),
(5, 5, 2, '2024-07-20', '2024-07-27', 'confirmed', 5),
(6, 6, 3, '2024-08-15', '2024-08-19', 'pending', 6),
(7, 1, 4, '2024-06-15', '2024-06-21', 'pending', 7),
(8, 2, 5, '2024-07-10', '2024-07-18', 'confirmed', 8),
(9, 3, 6, '2024-08-05', '2024-08-10', 'pending', 9); 