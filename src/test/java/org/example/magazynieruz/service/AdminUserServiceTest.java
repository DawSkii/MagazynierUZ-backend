package org.example.magazynieruz.service;

import jakarta.persistence.EntityNotFoundException;
import org.example.magazynieruz.dto.user.AdminCreateUserRequest;
import org.example.magazynieruz.dto.user.AdminUpdateUserRequest;
import org.example.magazynieruz.dto.user.UserResponse;
import org.example.magazynieruz.model.Organisation;
import org.example.magazynieruz.model.Role;
import org.example.magazynieruz.model.User;
import org.example.magazynieruz.repository.OrganisationRepository;
import org.example.magazynieruz.repository.RoleRepository;
import org.example.magazynieruz.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.lang.reflect.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link AdminUserService}.
 * Verifies user management operations including creation, update, deletion and organisation assignment.
 */
@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrganisationRepository organisationRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminUserService adminUserService;

    private Organisation testOrganisation;
    private Role userRole;
    private Role adminRole;
    private User testUser;

    @BeforeEach
    void setUp() {
        testOrganisation = new Organisation();
        testOrganisation.setId(1L);
        testOrganisation.setName("Test Organisation");

        userRole = new Role();
        userRole.setId(1);
        userRole.setName("ROLE_USER");

        adminRole = new Role();
        adminRole.setId(2);
        adminRole.setName("ROLE_ADMIN");

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setOrganisation(testOrganisation);
        testUser.setRoles(Set.of(userRole));
        try {
            Field userIdField = User.class.getDeclaredField("userId");
            userIdField.setAccessible(true);
            userIdField.set(testUser, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Tests user creation with valid data and expects successful user creation with encoded password.
     */
    @Test
    void testCreateUser_Success() {
        AdminCreateUserRequest request = new AdminCreateUserRequest(
                "newuser", "password123", 1L, Set.of("ROLE_USER")
        );
        
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(organisationRepository.findById(1L)).thenReturn(Optional.of(testOrganisation));
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = adminUserService.createUser(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getUsername()).isEqualTo("newuser");
        assertThat(capturedUser.getPassword()).isEqualTo("encodedPassword");
    }

    /**
     * Tests user creation with existing username and expects IllegalArgumentException.
     */
    @Test
    void testCreateUser_UsernameAlreadyExists() {
        AdminCreateUserRequest request = new AdminCreateUserRequest(
                "existinguser", "password123", 1L, Set.of("ROLE_USER")
        );
        
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        assertThatThrownBy(() -> adminUserService.createUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
        
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Tests user creation with non-existent organisation and expects EntityNotFoundException.
     */
    @Test
    void testCreateUser_OrganisationNotFound() {
        AdminCreateUserRequest request = new AdminCreateUserRequest(
                "newuser", "password123", 999L, Set.of("ROLE_USER")
        );
        
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(organisationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.createUser(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Organisation not found");
    }

    /**
     * Tests retrieving all users and expects list of user responses.
     */
    @Test
    void testGetAllUsers_Success() {
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);

        List<UserResponse> responses = adminUserService.getAllUsers();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).username()).isEqualTo("testuser");
    }

    /**
     * Tests retrieving user by ID with valid ID and expects user response.
     */
    @Test
    void testGetUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserResponse response = adminUserService.getUserById(1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.username()).isEqualTo("testuser");
    }

    /**
     * Tests retrieving user by non-existent ID and expects EntityNotFoundException.
     */
    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.getUserById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    /**
     * Tests updating user with valid data and expects successful update.
     */
    @Test
    void testUpdateUser_Success() {
        AdminUpdateUserRequest request = new AdminUpdateUserRequest(
                "updateduser", "newpassword", 1L, Set.of("ROLE_ADMIN")
        );
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("updateduser")).thenReturn(false);
        when(organisationRepository.findById(1L)).thenReturn(Optional.of(testOrganisation));
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));
        when(passwordEncoder.encode("newpassword")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = adminUserService.updateUser(1L, request);

        assertThat(response).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    /**
     * Tests updating user with taken username and expects IllegalArgumentException.
     */
    @Test
    void testUpdateUser_UsernameAlreadyTaken() {
        AdminUpdateUserRequest request = new AdminUpdateUserRequest(
                "takenusername", null, null, null
        );
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("takenusername")).thenReturn(true);

        assertThatThrownBy(() -> adminUserService.updateUser(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already taken");
    }

    /**
     * Tests deleting user by ID and expects successful deletion.
     */
    @Test
    void testDeleteUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        adminUserService.deleteUser(1L);

        verify(userRepository).delete(testUser);
    }

    /**
     * Tests deleting non-existent user and expects EntityNotFoundException.
     */
    @Test
    void testDeleteUser_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.deleteUser(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    /**
     * Tests assigning user to organisation and expects successful assignment.
     */
    @Test
    void testAssignUserToOrganisation_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(organisationRepository.findById(1L)).thenReturn(Optional.of(testOrganisation));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = adminUserService.assignUserToOrganisation(1L, 1L);

        assertThat(response).isNotNull();
        verify(userRepository).save(testUser);
    }

    /**
     * Tests assigning non-existent user to organisation and expects EntityNotFoundException.
     */
    @Test
    void testAssignUserToOrganisation_UserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.assignUserToOrganisation(999L, 1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    /**
     * Tests assigning user to non-existent organisation and expects EntityNotFoundException.
     */
    @Test
    void testAssignUserToOrganisation_OrganisationNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(organisationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.assignUserToOrganisation(1L, 999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Organisation not found");
    }

    /**
     * Tests user creation without specifying roles and expects default ROLE_USER to be assigned.
     */
    @Test
    void testCreateUser_WithDefaultRole_WhenNoRolesProvided() {
        AdminCreateUserRequest request = new AdminCreateUserRequest(
                "newuser", "password123", 1L, null
        );
        
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(organisationRepository.findById(1L)).thenReturn(Optional.of(testOrganisation));
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse response = adminUserService.createUser(request);

        assertThat(response).isNotNull();
        verify(roleRepository).findByName("ROLE_USER");
    }
}
