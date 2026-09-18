package com.shashi.minipay.entity;

/**
 * UserRole defines the roles available in the system.
 * Used for role-based access control (RBAC).
 *
 * CUSTOMER: Regular user who can create orders and make payments
 * ADMIN: Administrative user with elevated privileges
 */
public enum UserRole {
    CUSTOMER,
    ADMIN
}
