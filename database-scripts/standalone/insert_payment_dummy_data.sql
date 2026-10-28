-- insert_payment_dummy_data.sql
-- Inserts sample data for testing the Payment Service
-- Run this after creating the schema

-- Note: These are BCrypt hashed passwords
-- For reference:
-- - "Password123" -> $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH
-- - "SecurePass123" -> $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH
-- In production, generate these using BCrypt.hashpw()

-- Insert sample users
INSERT INTO payment.users (username, email, password_hash, role, created_at, updated_at) VALUES
    ('testuser', 'testuser@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'CUSTOMER', CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '5 days'),
    ('admin', 'admin@minipay.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'ADMIN', CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP - INTERVAL '10 days'),
    ('john_doe', 'john.doe@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'CUSTOMER', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '3 days'),
    ('jane_smith', 'jane.smith@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'CUSTOMER', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'),
    ('bob_wilson', 'bob.wilson@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'CUSTOMER', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day')
ON CONFLICT (username) DO NOTHING;

-- Insert sample payments
INSERT INTO payment.payments (amount, order_id, user_id, currency, provider_transaction_id, idempotency_key, status, created_at, updated_at) VALUES
    (100.50, 'ORD-2024-001', 'testuser', 'USD', 'ch_1234567890', 'idemp-key-001', 'PENDING', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day'),
    (250.00, 'ORD-2024-002', 'testuser', 'USD', 'ch_9876543210', 'idemp-key-002', 'COMPLETED', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'),
    (75.25, 'ORD-2024-003', 'john_doe', 'USD', NULL, 'idemp-key-003', 'FAILED', CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '3 days'),
    (500.00, 'ORD-2024-004', 'jane_smith', 'EUR', 'ch_5678901234', 'idemp-key-004', 'PROCESSING', CURRENT_TIMESTAMP - INTERVAL '5 hours', CURRENT_TIMESTAMP - INTERVAL '5 hours'),
    (150.75, 'ORD-2024-005', 'testuser', 'USD', 'ch_3456789012', 'idemp-key-005', 'COMPLETED', CURRENT_TIMESTAMP - INTERVAL '1 hour', CURRENT_TIMESTAMP - INTERVAL '1 hour'),
    (200.00, 'ORD-2024-006', 'bob_wilson', 'USD', 'ch_2345678901', 'idemp-key-006', 'REFUNDED', CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '2 hours')
ON CONFLICT (idempotency_key) DO NOTHING;
