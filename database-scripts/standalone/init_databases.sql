-- init_databases.sql
-- Creates the databases for the MiniPay project
-- Run this as the postgres superuser

-- Create databases if they don't exist
CREATE DATABASE minipay_order;
CREATE DATABASE minipay_payment;

-- Grant privileges (optional - if using a dedicated user)
-- Uncomment and modify if you want to use a dedicated database user
/*
CREATE USER minipay_user WITH PASSWORD 'minipay_password';
GRANT ALL PRIVILEGES ON DATABASE minipay_order TO minipay_user;
GRANT ALL PRIVILEGES ON DATABASE minipay_payment TO minipay_user;
*/

-- Verify databases were created
\l minipay_order
\l minipay_payment
