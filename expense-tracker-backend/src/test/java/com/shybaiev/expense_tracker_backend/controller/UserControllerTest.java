package com.shybaiev.expense_tracker_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shybaiev.expense_tracker_backend.dto.UserDto;
import com.shybaiev.expense_tracker_backend.dto.UserCreateUpdateDto;
import com.shybaiev.expense_tracker_backend.entity.User;
import com.shybaiev.expense_tracker_backend.entity.Role;
import com.shybaiev.expense_tracker_backend.mapper.ExpenseMapper;
import com.shybaiev.expense_tracker_backend.mapper.UserMapper;
import com.shybaiev.expense_tracker_backend.service.ExpenseService;
import com.shybaiev.expense_tracker_backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ExpenseService expenseService;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private ExpenseMapper expenseMapper;

    private User user1;
    private User user2;

    private UserDto userDto1;
    private UserDto userDto2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1L);
        user1.setEmail("admin@test.com");
        user1.setRole(Role.ADMIN);

        user2 = new User();
        user2.setId(2L);
        user2.setEmail("user@test.com");
        user2.setRole(Role.USER);

        userDto1 = new UserDto();
        userDto1.setId(1L);
        userDto1.setEmail("admin@test.com");

        userDto2 = new UserDto();
        userDto2.setId(2L);
        userDto2.setEmail("user@test.com");
    }


    @Test
    @WithMockUser
    void testCreateUser() throws Exception {

        UserCreateUpdateDto createDto = new UserCreateUpdateDto();
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

        when(userMapper.updateToEntity(any(UserCreateUpdateDto.class))).thenReturn(entity);
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
    @WithMockUser
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
    @WithMockUser
    void testGetUserByIdNotFound() throws Exception {
        when(userService.getUserById(42L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/users/42"))
                .andExpect(status().isNotFound());
    }


    @Test
    void testGetAllUsers_AdminAccess() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(user1, user2));
        when(userMapper.toDto(user1)).thenReturn(userDto1);
        when(userMapper.toDto(user2)).thenReturn(userDto2);

        mockMvc.perform(get("/users")
                        .principal(() -> "admin@test.com")
                        .with(user("admin@test.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].email").value("admin@test.com"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].email").value("user@test.com"));

        verify(userService).getAllUsers();
        verify(userMapper).toDto(user1);
        verify(userMapper).toDto(user2);
    }

    @Test
    void testGetAllUsers_UserAccessDenied() throws Exception {
        mockMvc.perform(get("/users")
                        .with(user("user@test.com").roles("USER")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
        verifyNoInteractions(userMapper);
    }
}
