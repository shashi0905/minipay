package com.shashi.minipay.repository;

import com.shashi.minipay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * UserRepository handles database operations for User entity.
 *
 * KEY CONCEPTS:
 * - Extends JpaRepository<User, UUID>: Provides CRUD operations automatically
 * - First type parameter (User): Entity type
 * - Second type parameter (UUID): Primary key type
 *
 * QUERY METHODS:
 * Spring Data JPA uses METHOD NAMING CONVENTION to derive SQL queries:
 * - findBy<FieldName>: SELECT * WHERE <fieldName> = ?
 * - Optional<T>: Returns empty if not found (avoid null pointer exceptions)
 *
 * Examples in this interface:
 * - findByUsername: Derived query -> SELECT * FROM users WHERE username = ?
 * - findByEmail: Derived query -> SELECT * FROM users WHERE email = ?
 *
 * Spring Data JPA automatically generates implementations at runtime!
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by username.
     * Returns Optional to handle "not found" case gracefully.
     *
     * SQL Generated: SELECT * FROM users WHERE username = ?
     *
     * @param username the username to search for
     * @return Optional containing User if found, empty otherwise
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email.
     * Returns Optional to handle "not found" case gracefully.
     *
     * SQL Generated: SELECT * FROM users WHERE email = ?
     *
     * @param email the email to search for
     * @return Optional containing User if found, empty otherwise
     */
    Optional<User> findByEmail(String email);
}
