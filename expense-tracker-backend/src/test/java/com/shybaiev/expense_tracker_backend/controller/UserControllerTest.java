package com.shybaiev.expense_tracker_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shybaiev.expense_tracker_backend.configuration.SecurityTestConfig;
import com.shybaiev.expense_tracker_backend.dto.UserDto;
import com.shybaiev.expense_tracker_backend.dto.UserUpdateDto;
import com.shybaiev.expense_tracker_backend.entity.User;
import com.shybaiev.expense_tracker_backend.entity.Role;
import com.shybaiev.expense_tracker_backend.mapper.UserMapper;
import com.shybaiev.expense_tracker_backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityTestConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserMapper userMapper;

    @Test
    void testCreateUser() throws Exception {

        UserUpdateDto createDto = new UserUpdateDto();
        createDto.setUsername("testUser");
        createDto.setEmail("test@email.com");
        createDto.setPassword("pass123");
        createDto.setRole(Role.USER);


        User entity = new User();
        entity.setId(1L);
        entity.setUsername("testUser");
        entity.setEmail("test@email.com");

        UserDto dto = new UserDto();
        dto.setId(1L);
        dto.setUsername("testUser");
        dto.setEmail("test@email.com");
        dto.setRole(Role.USER);
        dto.setEnabled(true);

        when(userMapper.updateToEntity(any(UserUpdateDto.class))).thenReturn(entity);
        when(userService.createUser(any(User.class))).thenReturn(entity);
        when(userMapper.toDto(entity)).thenReturn(dto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/users/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.email").value("test@email.com"));
    }

    @Test
    void testGetUserByIdFound() throws Exception {
        User entity = new User();
        entity.setId(1L);
        entity.setUsername("john");
        entity.setEmail("john@example.com");

        UserDto dto = new UserDto();
        dto.setId(1L);
        dto.setUsername("john");
        dto.setEmail("john@example.com");

        when(userService.getUserById(1L)).thenReturn(Optional.of(entity));
        when(userMapper.toDto(entity)).thenReturn(dto);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("john"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void testGetUserByIdNotFound() throws Exception {
        when(userService.getUserById(42L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/users/42"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllUsers() throws Exception {
        User entity = new User();
        entity.setId(1L);
        entity.setUsername("john");
        entity.setEmail("john@example.com");

        UserDto dto = new UserDto();
        dto.setId(1L);
        dto.setUsername("john");
        dto.setEmail("john@example.com");

        when(userService.getAllUsers()).thenReturn(List.of(entity));
        when(userMapper.toDto(entity)).thenReturn(dto);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].username").value("john"));
    }
}
