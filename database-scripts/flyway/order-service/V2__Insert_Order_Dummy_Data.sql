-- V2__Insert_Order_Dummy_Data.sql
-- Inserts sample data for testing the Order Service

-- Insert sample orders
INSERT INTO orders.orders (order_number, user_id, total_amount, currency, status, payment_id, created_at, updated_at) VALUES
    ('ORD-2024-001', 'user123', 100.50, 'USD', 'PENDING', NULL, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day'),
    ('ORD-2024-002', 'user123', 250.00, 'USD', 'PAID', 'pay_12345', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'),
    ('ORD-2024-003', 'user456', 75.25, 'USD', 'CANCELLED', NULL, CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '3 days'),
    ('ORD-2024-004', 'user789', 500.00, 'EUR', 'PENDING', NULL, CURRENT_TIMESTAMP - INTERVAL '5 hours', CURRENT_TIMESTAMP - INTERVAL '5 hours'),
    ('ORD-2024-005', 'user123', 150.75, 'USD', 'PAID', 'pay_67890', CURRENT_TIMESTAMP - INTERVAL '1 hour', CURRENT_TIMESTAMP - INTERVAL '1 hour')
ON CONFLICT (order_number) DO NOTHING;

-- Insert sample order items for order ORD-2024-001
INSERT INTO orders.order_items (order_id, product_id, product_name, quantity, unit_price, total_price)
SELECT 
    id,
    'PROD-001',
    'Wireless Mouse',
    2,
    25.00,
    50.00
FROM orders.orders 
WHERE order_number = 'ORD-2024-001'
ON CONFLICT DO NOTHING;

INSERT INTO orders.order_items (order_id, product_id, product_name, quantity, unit_price, total_price)
SELECT 
    id,
    'PROD-002',
    'USB-C Cable',
    1,
    50.50,
    50.50
FROM orders.orders 
WHERE order_number = 'ORD-2024-001'
ON CONFLICT DO NOTHING;

-- Insert sample order items for order ORD-2024-002
INSERT INTO orders.order_items (order_id, product_id, product_name, quantity, unit_price, total_price)
SELECT 
    id,
    'PROD-003',
    'Mechanical Keyboard',
    1,
    150.00,
    150.00
FROM orders.orders 
WHERE order_number = 'ORD-2024-002'
ON CONFLICT DO NOTHING;

INSERT INTO orders.order_items (order_id, product_id, product_name, quantity, unit_price, total_price)
SELECT 
    id,
    'PROD-004',
    'Monitor Stand',
    2,
    50.00,
    100.00
FROM orders.orders 
WHERE order_number = 'ORD-2024-002'
ON CONFLICT DO NOTHING;

-- Insert sample order items for order ORD-2024-003
INSERT INTO orders.order_items (order_id, product_id, product_name, quantity, unit_price, total_price)
SELECT 
    id,
    'PROD-005',
    'Webcam HD',
    1,
    75.25,
    75.25
FROM orders.orders 
WHERE order_number = 'ORD-2024-003'
ON CONFLICT DO NOTHING;

-- Insert sample order items for order ORD-2024-004
INSERT INTO orders.order_items (order_id, product_id, product_name, quantity, unit_price, total_price)
SELECT 
    id,
    'PROD-006',
    'Laptop Sleeve',
    2,
    100.00,
    200.00
FROM orders.orders 
WHERE order_number = 'ORD-2024-004'
ON CONFLICT DO NOTHING;

INSERT INTO orders.order_items (order_id, product_id, product_name, quantity, unit_price, total_price)
SELECT 
    id,
    'PROD-007',
    'Wireless Charger',
    3,
    100.00,
    300.00
FROM orders.orders 
WHERE order_number = 'ORD-2024-004'
ON CONFLICT DO NOTHING;

-- Insert sample order items for order ORD-2024-005
INSERT INTO orders.order_items (order_id, product_id, product_name, quantity, unit_price, total_price)
SELECT 
    id,
    'PROD-008',
    'Bluetooth Speaker',
    1,
    75.00,
    75.00
FROM orders.orders 
WHERE order_number = 'ORD-2024-005'
ON CONFLICT DO NOTHING;

INSERT INTO orders.order_items (order_id, product_id, product_name, quantity, unit_price, total_price)
SELECT 
    id,
    'PROD-009',
    'Phone Stand',
    3,
    25.25,
    75.75
FROM orders.orders 
WHERE order_number = 'ORD-2024-005'
ON CONFLICT DO NOTHING;
