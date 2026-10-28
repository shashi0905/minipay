# Database Scripts for MiniPay

This directory contains database initialization and migration scripts for the MiniPay project.

## Services and Databases

- **Order Service**: PostgreSQL database `minipay_order`, schema `orders`
- **Payment Service**: PostgreSQL database `minipay_payment`, schema `payment`

## Options

### Option 1: Flyway Migrations (Recommended)

Flyway provides version-controlled database migrations. To use:

1. Add Flyway dependency to each service's `pom.xml`:
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

2. Configure Flyway in `application.yml`:
```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    schemas: orders  # or 'payment' for payment-service
```

3. Place migration files in `src/main/resources/db/migration/`
   - Naming convention: `V{version}__{description}.sql`
   - Example: `V1__Create_Orders_Schema.sql`

4. Disable JPA auto-DDL in `application.yml`:
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # or 'none'
```

### Option 2: Standalone SQL Scripts

For quick setup without Flyway, use the SQL scripts in the `standalone/` directory.

Run scripts manually:
```bash
psql -U postgres -d minipay_order -f standalone/create_order_schema.sql
psql -U postgres -d minipay_payment -f standalone/create_payment_schema.sql
psql -U postgres -d minipay_order -f standalone/insert_order_dummy_data.sql
psql -U postgres -d minipay_payment -f standalone/insert_payment_dummy_data.sql
```

## Database Setup

### Create Databases

```sql
-- Create databases
CREATE DATABASE minipay_order;
CREATE DATABASE minipay_payment;
```

### Create Users (if needed)

```sql
-- Create user (if not using default postgres user)
CREATE USER minipay_user WITH PASSWORD 'minipay_password';
GRANT ALL PRIVILEGES ON DATABASE minipay_order TO minipay_user;
GRANT ALL PRIVILEGES ON DATABASE minipay_payment TO minipay_user;
```

## Migration Files

### Order Service

- `V1__Create_Orders_Schema.sql` - Creates orders and order_items tables
- `V2__Insert_Order_Dummy_Data.sql` - Inserts sample orders

### Payment Service

- `V1__Create_Payment_Schema.sql` - Creates users and payments tables
- `V2__Insert_Payment_Dummy_Data.sql` - Inserts sample users and payments

## Notes

- All monetary values use DECIMAL(10,2) for precision
- Currency codes are 3-character ISO codes
- UUIDs are used for primary keys
- Timestamps use TIMESTAMP WITH TIME ZONE
- Passwords are BCrypt hashed (never store plain text)
