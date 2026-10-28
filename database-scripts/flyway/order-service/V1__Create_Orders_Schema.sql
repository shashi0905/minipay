-- V1__Create_Orders_Schema.sql
-- Creates the orders schema and tables for the Order Service

-- Create schema if it doesn't exist
CREATE SCHEMA IF NOT EXISTS orders;

-- Create orders table
CREATE TABLE IF NOT EXISTS orders.orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number VARCHAR(255) NOT NULL UNIQUE,
    user_id VARCHAR(255) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(50) NOT NULL,
    payment_id VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_orders_total_amount_positive CHECK (total_amount > 0),
    CONSTRAINT chk_orders_currency_length CHECK (LENGTH(currency) = 3)
);

-- Create order_items table
CREATE TABLE IF NOT EXISTS orders.order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    product_id VARCHAR(255) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    CONSTRAINT chk_order_items_quantity_positive CHECK (quantity > 0),
    CONSTRAINT chk_order_items_unit_price_positive CHECK (unit_price > 0),
    CONSTRAINT chk_order_items_total_price_positive CHECK (total_price > 0),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders.orders(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders.orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders.orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_order_number ON orders.orders(order_number);
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON orders.order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_product_id ON orders.order_items(product_id);

-- Create trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION orders.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_orders_updated_at BEFORE UPDATE ON orders.orders
    FOR EACH ROW EXECUTE FUNCTION orders.update_updated_at_column();

CREATE TRIGGER update_order_items_updated_at BEFORE UPDATE ON orders.order_items
    FOR EACH ROW EXECUTE FUNCTION orders.update_updated_at_column();

-- Add comment to tables
COMMENT ON TABLE orders.orders IS 'Stores order information including status, amount, and payment details';
COMMENT ON TABLE orders.order_items IS 'Stores individual items within an order';
