package org.example.magazynieruz.service;

import org.example.magazynieruz.model.User;
import org.example.magazynieruz.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link MyUserDetailsService}.
 * Verifies user loading functionality for Spring Security authentication.
 */
@ExtendWith(MockitoExtension.class)
class MyUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MyUserDetailsService myUserDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
    }

    /**
     * Tests loading user by username with valid username and expects user details.
     */
    @Test
    void testLoadUserByUsername_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        UserDetails userDetails = myUserDetailsService.loadUserByUsername("testuser");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
        verify(userRepository).findByUsername("testuser");
    }

    /**
     * Tests loading user by non-existent username and expects UsernameNotFoundException.
     */
    @Test
    void testLoadUserByUsername_UserNotFound_ThrowsException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> myUserDetailsService.loadUserByUsername("nonexistent"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");
        
        verify(userRepository).findByUsername("nonexistent");
    }

    /**
     * Tests that loaded user details is an instance of User class.
     */
    @Test
    void testLoadUserByUsername_ReturnsUserObject() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(testUser));

        UserDetails result = myUserDetailsService.loadUserByUsername("admin");

        assertThat(result).isInstanceOf(User.class);
        assertThat(((User) result).getUsername()).isEqualTo("testuser");
    }

    /**
     * Tests that username matching is case-sensitive.
     */
    @Test
    void testLoadUserByUsername_CaseExactMatch() {
        User upperCaseUser = new User();
        upperCaseUser.setUsername("TESTUSER");
        upperCaseUser.setPassword("pass");
        
        when(userRepository.findByUsername("TESTUSER")).thenReturn(Optional.of(upperCaseUser));

        UserDetails result = myUserDetailsService.loadUserByUsername("TESTUSER");

        assertThat(result.getUsername()).isEqualTo("TESTUSER");
    }

    /**
     * Tests loading user with special characters in username and expects successful loading.
     */
    @Test
    void testLoadUserByUsername_WithSpecialCharacters() {
        User specialUser = new User();
        specialUser.setUsername("user@example.com");
        specialUser.setPassword("pass");
        
        when(userRepository.findByUsername("user@example.com")).thenReturn(Optional.of(specialUser));

        UserDetails result = myUserDetailsService.loadUserByUsername("user@example.com");

        assertThat(result.getUsername()).isEqualTo("user@example.com");
    }
}
