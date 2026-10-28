-- create_payment_schema.sql
-- Creates the payment schema and tables for the Payment Service
-- Run this script to initialize the database schema

-- Create schema if it doesn't exist
CREATE SCHEMA IF NOT EXISTS payment;

-- Create users table
CREATE TABLE IF NOT EXISTS payment.users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'CUSTOMER',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_users_username_length CHECK (LENGTH(username) >= 3 AND LENGTH(username) <= 50),
    CONSTRAINT chk_users_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT chk_users_role_valid CHECK (role IN ('CUSTOMER', 'ADMIN'))
);

-- Create payments table
CREATE TABLE IF NOT EXISTS payment.payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    amount DECIMAL(10,2) NOT NULL,
    order_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    provider_transaction_id VARCHAR(255),
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_payments_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_payments_currency_length CHECK (LENGTH(currency) = 3),
    CONSTRAINT chk_payments_status_valid CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'REFUNDED'))
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_payment_users_username ON payment.users(username);
CREATE INDEX IF NOT EXISTS idx_payment_users_email ON payment.users(email);
CREATE INDEX IF NOT EXISTS idx_payments_order_id ON payment.payments(order_id);
CREATE INDEX IF NOT EXISTS idx_payments_user_id ON payment.payments(user_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payment.payments(status);
CREATE INDEX IF NOT EXISTS idx_payments_idempotency_key ON payment.payments(idempotency_key);

-- Create trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION payment.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

DROP TRIGGER IF EXISTS update_users_updated_at ON payment.users;
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON payment.users
    FOR EACH ROW EXECUTE FUNCTION payment.update_updated_at_column();

DROP TRIGGER IF EXISTS update_payments_updated_at ON payment.payments;
CREATE TRIGGER update_payments_updated_at BEFORE UPDATE ON payment.payments
    FOR EACH ROW EXECUTE FUNCTION payment.update_updated_at_column();

-- Add comment to tables
COMMENT ON TABLE payment.users IS 'Stores user authentication and profile information';
COMMENT ON TABLE payment.payments IS 'Stores payment transactions and their status';
