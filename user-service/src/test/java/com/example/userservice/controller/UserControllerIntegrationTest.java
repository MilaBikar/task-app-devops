package com.example.userservice.controller;

import com.example.userservice.dto.UserDTO;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("INTEGRATION TESTS - UserController")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create user via POST /api/users")
    void shouldCreateUserViaAPI() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("integration_test");
        userDTO.setEmail("integration@example.com");
        userDTO.setFullName("Integration Test User");

        String userJson = objectMapper.writeValueAsString(userDTO);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("integration_test"))
                .andExpect(jsonPath("$.email").value("integration@example.com"))
                .andExpect(jsonPath("$.fullName").value("Integration Test User"))
                .andExpect(jsonPath("$.createdAt").exists());

        User savedUser = userRepository.findByUsername("integration_test").orElseThrow();
        assertEquals("integration@example.com", savedUser.getEmail());
    }

    @Test
    @DisplayName("Should return 409 when username already exists")
    void shouldReturn409WhenUsernameExists() throws Exception {
        User existingUser = new User();
        existingUser.setUsername("existing");
        existingUser.setEmail("existing@example.com");
        existingUser.setFullName("Existing User");
        userRepository.save(existingUser);

        UserDTO newUser = new UserDTO();
        newUser.setUsername("existing");
        newUser.setEmail("newemail@example.com");
        newUser.setFullName("New User");

        String userJson = objectMapper.writeValueAsString(newUser);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Username already exists")));
    }

    @Test
    @DisplayName("Should return 400 when email is invalid")
    void shouldReturn400WhenEmailInvalid() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testuser");
        userDTO.setEmail("invalid-email");
        userDTO.setFullName("Test User");

        String userJson = objectMapper.writeValueAsString(userDTO);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get all users via GET /api/users")
    void shouldGetAllUsers() throws Exception {
        User user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setFullName("User One");
        userRepository.save(user1);

        User user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setFullName("User Two");
        userRepository.save(user2);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[1].username").value("user2"));
    }

    @Test
    @DisplayName("Should get user by ID via GET /api/users/{id}")
    void shouldGetUserById() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        User savedUser = userRepository.save(user);

        mockMvc.perform(get("/api/users/" + savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("Should return 404 when user not found by ID")
    void shouldReturn404WhenUserNotFound() throws Exception {
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("User not found")));
    }

    @Test
    @DisplayName("Should update user via PUT /api/users/{id}")
    void shouldUpdateUser() throws Exception {
        User user = new User();
        user.setUsername("oldname");
        user.setEmail("old@example.com");
        user.setFullName("Old Name");
        User savedUser = userRepository.save(user);

        UserDTO updateDTO = new UserDTO();
        updateDTO.setUsername("newname");
        updateDTO.setEmail("new@example.com");
        updateDTO.setFullName("New Name");

        String updateJson = objectMapper.writeValueAsString(updateDTO);

        mockMvc.perform(put("/api/users/" + savedUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newname"))
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.fullName").value("New Name"));

        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertEquals("newname", updatedUser.getUsername());
    }

    @Test
    @DisplayName("Should delete user via DELETE /api/users/{id}")
    void shouldDeleteUser() throws Exception {
        User user = new User();
        user.setUsername("todelete");
        user.setEmail("delete@example.com");
        user.setFullName("To Delete");
        User savedUser = userRepository.save(user);

        mockMvc.perform(delete("/api/users/" + savedUser.getId()))
                .andExpect(status().isNoContent());

        assertFalse(userRepository.existsById(savedUser.getId()));
    }

    @Test
    @DisplayName("Should get user by username via GET /api/users/username/{username}")
    void shouldGetUserByUsername() throws Exception {
        User user = new User();
        user.setUsername("findme");
        user.setEmail("findme@example.com");
        user.setFullName("Find Me");
        userRepository.save(user);

        mockMvc.perform(get("/api/users/username/findme"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("findme"))
                .andExpect(jsonPath("$.email").value("findme@example.com"));
    }

    @Test
    @DisplayName("Should return 400 when username is blank")
    void shouldReturn400WhenUsernameBlank() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("");
        userDTO.setEmail("test@example.com");
        userDTO.setFullName("Test");

        String userJson = objectMapper.writeValueAsString(userDTO);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isBadRequest());
    }
}