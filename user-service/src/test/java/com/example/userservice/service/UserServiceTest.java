package com.example.userservice.service;

import com.example.userservice.dto.UserDTO;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UNIT TESTS - UserService")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserDTO testUserDTO;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUserDTO = new UserDTO();
        testUserDTO.setUsername("testuser");
        testUserDTO.setEmail("test@example.com");
        testUserDTO.setFullName("Test User");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");
    }

    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUserSuccessfully() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDTO result = userService.createUser(testUserDTO);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test User", result.getFullName());

        verify(userRepository, times(1)).existsByUsername("testuser");
        verify(userRepository, times(1)).existsByEmail("test@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void shouldThrowExceptionWhenUsernameExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        UserService.DuplicateResourceException exception = assertThrows(
                UserService.DuplicateResourceException.class,
                () -> userService.createUser(testUserDTO)
        );

        assertEquals("Username already exists: testuser", exception.getMessage());
        verify(userRepository, times(1)).existsByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void shouldThrowExceptionWhenEmailExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        UserService.DuplicateResourceException exception = assertThrows(
                UserService.DuplicateResourceException.class,
                () -> userService.createUser(testUserDTO)
        );

        assertEquals("Email already exists: test@example.com", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should get user by ID successfully")
    void shouldGetUserByIdSuccessfully() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserDTO result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when user not found by ID")
    void shouldThrowExceptionWhenUserNotFoundById() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        UserService.ResourceNotFoundException exception = assertThrows(
                UserService.ResourceNotFoundException.class,
                () -> userService.getUserById(999L)
        );

        assertEquals("User not found with id: 999", exception.getMessage());
    }

    @Test
    @DisplayName("Should get all users successfully")
    void shouldGetAllUsersSuccessfully() {
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");

        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));

        List<UserDTO> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        assertEquals("user2", result.get(1).getUsername());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUserSuccessfully() {
        UserDTO updateDTO = new UserDTO();
        updateDTO.setUsername("updateduser");
        updateDTO.setEmail("updated@example.com");
        updateDTO.setFullName("Updated Name");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("updateduser")).thenReturn(false);
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDTO result = userService.updateUser(1L, updateDTO);

        assertNotNull(result);
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should delete user successfully")
    void shouldDeleteUserSuccessfully() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent user")
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        when(userRepository.existsById(999L)).thenReturn(false);

        UserService.ResourceNotFoundException exception = assertThrows(
                UserService.ResourceNotFoundException.class,
                () -> userService.deleteUser(999L)
        );

        assertEquals("User not found with id: 999", exception.getMessage());
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should get user by username successfully")
    void shouldGetUserByUsernameSuccessfully() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        UserDTO result = userService.getUserByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
    }
}