-- Insert assistance requests data
INSERT INTO assistance_requests (request_id, user_id, issue_description, priority, status, resolution_time, timestamp) VALUES
(1, 2, 'Need help with visa application for Japan trip', 'high', 'pending', NULL, '2024-03-10T10:30:00Z'),
(2, 4, 'Flight change request due to personal emergency', 'high', 'completed', '2 hours', '2024-03-08T14:20:00Z'),
(3, 6, 'Hotel upgrade request for anniversary celebration', 'medium', 'pending', NULL, '2024-03-12T09:15:00Z'); 