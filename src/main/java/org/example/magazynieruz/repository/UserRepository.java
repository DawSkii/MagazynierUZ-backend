package org.example.magazynieruz.repository;

import org.example.magazynieruz.model.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity data access.
 * Provides CRUD operations and custom queries for user management and authentication.
 */
@Repository
public interface UserRepository extends CrudRepository<User, Long>{
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
