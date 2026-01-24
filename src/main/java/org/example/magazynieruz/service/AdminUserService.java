package org.example.magazynieruz.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.magazynieruz.dto.user.AdminCreateUserRequest;
import org.example.magazynieruz.dto.user.AdminUpdateUserRequest;
import org.example.magazynieruz.dto.user.UserResponse;
import org.example.magazynieruz.model.Organisation;
import org.example.magazynieruz.model.Role;
import org.example.magazynieruz.model.User;
import org.example.magazynieruz.repository.OrganisationRepository;
import org.example.magazynieruz.repository.RoleRepository;
import org.example.magazynieruz.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for administrative user management operations.
 * Provides CRUD operations for users that can only be performed by administrators.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {

    private final UserRepository userRepository;
    private final OrganisationRepository organisationRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates a new user with specified organisation and roles.
     *
     * @param request the user creation request containing username, password, organisation, and roles
     * @return UserResponse containing the created user details
     * @throws IllegalArgumentException if username already exists
     * @throws EntityNotFoundException if organisation not found
     */
    @Transactional
    public UserResponse createUser(AdminCreateUserRequest request) {
        log.info("Admin creating user: {}", request.username());
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("User with username " + request.username() + " already exists");
        }
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        if (request.organisationId() != null) {
            Organisation organisation = organisationRepository.findById(request.organisationId())
                    .orElseThrow(() -> new EntityNotFoundException("Organisation not found with ID: " + request.organisationId()));
            user.setOrganisation(organisation);
        }
        Set<Role> roles = getRolesFromNames(request.roleNames());
        user.setRoles(roles);
        User savedUser = userRepository.save(user);
        log.info("User created with ID: {}", savedUser.getUserId());
        return mapToResponse(savedUser);
    }

    /**
     * Retrieves all users in the system.
     *
     * @return list of all users
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.info("Admin fetching all users");
        List<User> users = (List<User>) userRepository.findAll();
        return users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a specific user by ID.
     *
     * @param id the user ID
     * @return UserResponse containing user details
     * @throws EntityNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.info("Admin fetching user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));
        return mapToResponse(user);
    }

    /**
     * Updates an existing user's details including username, password, organisation, and roles.
     *
     * @param id the user ID
     * @param request the update request with fields to modify
     * @return UserResponse containing updated user details
     * @throws EntityNotFoundException if user or organisation not found
     * @throws IllegalArgumentException if new username is already taken
     */
    @Transactional
    public UserResponse updateUser(Long id, AdminUpdateUserRequest request) {
        log.info("Admin updating user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));
        if (request.username() != null && !request.username().isBlank()) {
            if (!user.getUsername().equals(request.username()) &&
                userRepository.existsByUsername(request.username())) {
                throw new IllegalArgumentException("Username " + request.username() + " is already taken");
            }
            user.setUsername(request.username());
        }
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        if (request.organisationId() != null) {
            Organisation organisation = organisationRepository.findById(request.organisationId())
                    .orElseThrow(() -> new EntityNotFoundException("Organisation not found with ID: " + request.organisationId()));
            user.setOrganisation(organisation);
        }
        if (request.roleNames() != null && !request.roleNames().isEmpty()) {
            Set<Role> roles = getRolesFromNames(request.roleNames());
            user.setRoles(roles);
        }
        User updatedUser = userRepository.save(user);
        log.info("User updated with ID: {}", updatedUser.getUserId());
        return mapToResponse(updatedUser);
    }

    /**
     * Deletes a user from the system.
     *
     * @param id the user ID
     * @throws EntityNotFoundException if user not found
     */
    @Transactional
    public void deleteUser(Long id) {
        log.info("Admin deleting user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));

        userRepository.delete(user);
        log.info("User deleted with ID: {}", id);
    }

    /**
     * Assigns a user to a specific organisation.
     *
     * @param userId the user ID
     * @param organisationId the organisation ID
     * @return UserResponse containing updated user details
     * @throws EntityNotFoundException if user or organisation not found
     */
    @Transactional
    public UserResponse assignUserToOrganisation(Long userId, Long organisationId) {
        log.info("Admin assigning user {} to organisation {}", userId, organisationId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found with ID: " + organisationId));

        user.setOrganisation(organisation);
        User updatedUser = userRepository.save(user);

        log.info("User {} assigned to organisation {}", userId, organisationId);
        return mapToResponse(updatedUser);
    }

    private Set<Role> getRolesFromNames(Set<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            // Default to ROLE_USER if no roles specified
            Role defaultRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Default role ROLE_USER not found"));
            return Set.of(defaultRole);
        }

        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleName));
            roles.add(role);
        }
        return roles;
    }

    /**
     * Maps User entity to UserResponse DTO.
     *
     * @param user the user entity
     * @return UserResponse DTO
     */
    private UserResponse mapToResponse(User user) {
        Long orgId = user.getOrganisation() != null ? user.getOrganisation().getId() : null;
        String orgName = user.getOrganisation() != null ? user.getOrganisation().getName() : null;
        Set<String> roleNames = user.getAuthorities() != null
                ? user.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .collect(Collectors.toSet())
                : Set.of();
        return new UserResponse(
                user.getUserId(),
                user.getUsername(),
                orgId,
                orgName,
                roleNames
        );
    }
}
