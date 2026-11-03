package com.example.artship.features.user.service;

import com.example.artship.entities.user.model.User;
import com.example.artship.entities.user.model.UserRole;
import com.example.artship.entities.user.repository.UserRepository;
import com.example.artship.features.user.dto.RoleStatistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User(
                "testuser",
                "test@example.com",
                "hashed-password"
        );
    }

    @Test
    void findByUsernameShouldReturnUser() {
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));

        Optional<User> result =
                userService.findByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());

        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void findByEmailShouldReturnUser() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        Optional<User> result =
                userService.findByEmail("test@example.com");

        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    void saveShouldCallRepository() {
        when(userRepository.save(testUser)).thenReturn(testUser);

        User result = userService.save(testUser);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());

        verify(userRepository).save(testUser);
    }

    @Test
    void changeUserRoleShouldUpdateRole() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser))
                .thenReturn(testUser);

        User result = userService.changeUserRole(
                1L,
                UserRole.MODERATOR
        );

        assertEquals(UserRole.MODERATOR, result.getUserRole());
        verify(userRepository).save(testUser);
    }

    @Test
    void changeUserRoleShouldThrowWhenUserNotFound() {
        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.changeUserRole(
                        999L,
                        UserRole.ADMIN
                )
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void hasRoleShouldReturnTrueWhenRoleMatches() {
        testUser.setUserRole(UserRole.ADMIN);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(testUser));

        assertTrue(userService.hasRole(1L, UserRole.ADMIN));
    }

    @Test
    void hasRoleShouldReturnFalseWhenUserDoesNotExist() {
        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertFalse(userService.hasRole(999L, UserRole.ADMIN));
    }

    @Test
    void getRoleStatisticsShouldReturnCorrectCounts() {
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.countByUserRole(UserRole.ADMIN))
                .thenReturn(1L);
        when(userRepository.countByUserRole(UserRole.MODERATOR))
                .thenReturn(2L);
        when(userRepository.countByUserRole(UserRole.USER))
                .thenReturn(7L);

        RoleStatistics result = userService.getRoleStatistics();

        assertEquals(10L, result.getTotalUsers());
        assertEquals(1L, result.getAdminCount());
        assertEquals(2L, result.getModeratorCount());
        assertEquals(7L, result.getUserCount());
    }
}