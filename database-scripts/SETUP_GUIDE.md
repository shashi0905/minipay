# Database Setup Guide for MiniPay

This guide provides step-by-step instructions for setting up the databases for the MiniPay project.

## Prerequisites

- PostgreSQL 12 or higher installed and running
- Access to postgres superuser or a user with database creation privileges
- psql command-line tool or a PostgreSQL client (pgAdmin, DBeaver, etc.)

## Quick Setup (Standalone SQL Scripts)

### Step 1: Create Databases

```bash
# Connect to PostgreSQL as postgres user
psql -U postgres

# Run the initialization script
\i database-scripts/standalone/init_databases.sql

# Exit psql
\q
```

Or directly from command line:
```bash
psql -U postgres -f database-scripts/standalone/init_databases.sql
```

### Step 2: Create Schemas and Tables

```bash
# Create order service schema and tables
psql -U postgres -d minipay_order -f database-scripts/standalone/create_order_schema.sql

# Create payment service schema and tables
psql -U postgres -d minipay_payment -f database-scripts/standalone/create_payment_schema.sql
```

### Step 3: Insert Dummy Data

```bash
# Insert dummy data for order service
psql -U postgres -d minipay_order -f database-scripts/standalone/insert_order_dummy_data.sql

# Insert dummy data for payment service
psql -U postgres -d minipay_payment -f database-scripts/standalone/insert_payment_dummy_data.sql
```

### Step 4: Verify Setup

```bash
# Check order service tables
psql -U postgres -d minipay_order -c "\dt orders.*"

# Check payment service tables
psql -U postgres -d minipay_payment -c "\dt payment.*"

# Verify data
psql -U postgres -d minipay_order -c "SELECT * FROM orders.orders LIMIT 5;"
psql -U postgres -d minipay_payment -c "SELECT * FROM payment.users LIMIT 5;"
```

---

## Production Setup (Flyway Migrations - Recommended)

### Step 1: Add Flyway Dependencies

Add to each service's `pom.xml`:

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

### Step 2: Copy Migration Files

Copy the migration files to each service's resources directory:

```bash
# Order Service
cp database-scripts/flyway/order-service/V1__Create_Orders_Schema.sql order-service/src/main/resources/db/migration/
cp database-scripts/flyway/order-service/V2__Insert_Order_Dummy_Data.sql order-service/src/main/resources/db/migration/

# Payment Service
cp database-scripts/flyway/payment-service/V1__Create_Payment_Schema.sql payment-service/src/main/resources/db/migration/
cp database-scripts/flyway/payment-service/V2__Insert_Payment_Dummy_Data.sql payment-service/src/main/resources/db/migration/
```

### Step 3: Configure Flyway

Update `application.yml` in each service:

**Order Service (`order-service/src/main/resources/application.yml`):**
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # Change from 'update' to 'validate'
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    schemas: orders
```

**Payment Service (`payment-service/src/main/resources/application.yml`):**
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # Change from 'update' to 'validate'
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    schemas: payment
```

### Step 4: Create Databases

```bash
psql -U postgres -f database-scripts/standalone/init_databases.sql
```

### Step 5: Run Applications

Flyway will automatically run the migrations when you start the applications:

```bash
# Start order service
cd order-service
mvn spring-boot:run

# Start payment service
cd payment-service
mvn spring-boot:run
```

---

## Password Hashes for Dummy Data

The dummy data scripts use BCrypt-hashed passwords. The following credentials are pre-configured:

| Username | Password | Role |
|----------|----------|------|
| testuser | Password123 | CUSTOMER |
| admin | Password123 | ADMIN |
| john_doe | Password123 | CUSTOMER |
| jane_smith | Password123 | CUSTOMER |
| bob_wilson | Password123 | CUSTOMER |

**Note**: The hash in the scripts is a placeholder. For proper security, generate real BCrypt hashes:

### Option 1: Online Generator
Visit https://bcrypt-generator.com/ and generate hashes for your desired passwords.

### Option 2: Using Java
Use the provided utility:
```bash
cd database-scripts
javac -cp ../payment-service/target/classes generate_password_hash.java
java -cp .:../payment-service/target/classes generate_password_hash
```

### Option 3: Using Python
```python
import bcrypt
password = b"Password123"
hashed = bcrypt.hashpw(password, bcrypt.gensalt())
print(hashed.decode())
```

Update the `insert_payment_dummy_data.sql` file with the generated hashes.

---

## Troubleshooting

### Database Connection Issues

If you get connection errors, check:
1. PostgreSQL is running: `pg_isready`
2. Credentials in `application.yml` match your PostgreSQL setup
3. Database exists: `psql -U postgres -l`

### Schema Already Exists

If you get "schema already exists" errors, the scripts use `IF NOT EXISTS` so they should be safe. If you need to start fresh:

```bash
# Drop and recreate databases (CAUTION: This deletes all data!)
psql -U postgres -c "DROP DATABASE IF EXISTS minipay_order;"
psql -U postgres -c "DROP DATABASE IF EXISTS minipay_payment;"
psql -U postgres -f database-scripts/standalone/init_databases.sql
```

### Flyway Migration Failures

If Flyway fails to migrate:
1. Check the `flyway_schema_history` table for failed migrations
2. Fix the SQL in the migration file
3. For development, you can repair: `flyway repair`
4. Or drop the schema and start fresh

### Permission Issues

If you get permission errors, ensure your PostgreSQL user has the necessary privileges:

```sql
-- Grant all privileges on database
GRANT ALL PRIVILEGES ON DATABASE minipay_order TO postgres;
GRANT ALL PRIVILEGES ON DATABASE minipay_payment TO postgres;

-- Grant all privileges on schema
GRANT ALL ON SCHEMA orders TO postgres;
GRANT ALL ON SCHEMA payment TO postgres;
```

---

## Verification

After setup, verify the databases are working:

### Test Order Service

```bash
curl -X GET http://localhost:8083/api/orders/user/testuser
```

### Test Payment Service

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "testuser",
    "password": "Password123"
  }'
```

---

## Next Steps

1. Update password hashes in dummy data scripts with real BCrypt hashes
2. Configure Flyway for production deployments
3. Set up separate databases for staging and production
4. Implement database backup strategies
5. Consider adding database monitoring and alerting
