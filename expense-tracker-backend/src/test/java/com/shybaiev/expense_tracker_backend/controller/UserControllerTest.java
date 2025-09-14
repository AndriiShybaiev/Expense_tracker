package com.shybaiev.expense_tracker_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shybaiev.expense_tracker_backend.configuration.SecurityTestConfig;
import com.shybaiev.expense_tracker_backend.dto.UserDto;
import com.shybaiev.expense_tracker_backend.dto.UserCreateUpdateDto;
import com.shybaiev.expense_tracker_backend.entity.User;
import com.shybaiev.expense_tracker_backend.entity.Role;
import com.shybaiev.expense_tracker_backend.mapper.ExpenseMapper;
import com.shybaiev.expense_tracker_backend.mapper.UserMapper;
import com.shybaiev.expense_tracker_backend.security.CustomUserDetails;
import com.shybaiev.expense_tracker_backend.service.ExpenseService;
import com.shybaiev.expense_tracker_backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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
    private ExpenseService expenseService;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private ExpenseMapper expenseMapper;

    private User admin;
    private User user;

    private UserDto adminDto;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        admin = new User();
        admin.setId(1L);
        admin.setEmail("admin@test.com");
        admin.setUsername("admin");
        admin.setRole(Role.ADMIN);

        user = new User();
        user.setId(2L);
        user.setEmail("user@test.com");
        user.setUsername("user");
        user.setRole(Role.USER);

        adminDto = new UserDto();
        adminDto.setId(1L);
        adminDto.setEmail("admin@test.com");

        userDto = new UserDto();
        userDto.setId(2L);
        userDto.setEmail("user@test.com");
    }

    @Test
    void testUpdateUserSelf() throws Exception {
        UserCreateUpdateDto dto = new UserCreateUpdateDto();
        dto.setUsername("newUserName");
        dto.setEmail("newuser@test.com");
        dto.setPassword("newPass");
        dto.setRole(Role.ADMIN); // try to elevate role to admin

        when(userService.getUserById(2L)).thenReturn(Optional.of(user));
        when(userService.updateUser(eq(2L), any(User.class)))
                .thenAnswer(invocation -> {
                    User u = invocation.getArgument(1);
                    user.setUsername(u.getUsername());
                    user.setEmail(u.getEmail());
                    user.setPasswordHash(u.getPasswordHash());
                    // Роль не меняем
                    return user;
                });
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        CustomUserDetails principal = new CustomUserDetails(user);

        mockMvc.perform(patch("/users/2")
                        .with(authentication(
                                new UsernamePasswordAuthenticationToken(
                                        principal, principal.getPassword(), principal.getAuthorities()
                                )
                        ))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        // check that it si still USER
        assertEquals(Role.USER, user.getRole());
        assertEquals("newUserName", user.getUsername());
        assertEquals("newuser@test.com", user.getEmail());
    }

    @Test
    void testUpdateUserAdmin() throws Exception {
        UserCreateUpdateDto dto = new UserCreateUpdateDto();
        dto.setUsername("adminChanged");
        dto.setEmail("adminchanged@test.com");
        dto.setPassword("adminPass");
        dto.setRole(Role.ADMIN);

        when(userService.getUserById(2L)).thenReturn(Optional.of(user));
        when(userService.updateUser(eq(2L), any(User.class))).thenAnswer(invocation -> invocation.getArgument(1));
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        mockMvc.perform(patch("/users/2")
                        .with(user(new CustomUserDetails(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllExpensesSelf() throws Exception {
        when(userService.getUserById(2L)).thenReturn(Optional.of(user));
        when(expenseService.getAllExpensesForUser(user.getEmail())).thenReturn(List.of());

        mockMvc.perform(get("/users/2/expenses")
                        .with(user(new CustomUserDetails(user))))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllExpensesAdmin() throws Exception {
        when(userService.getUserById(2L)).thenReturn(Optional.of(user));
        when(expenseService.getAllExpensesForUser(user.getEmail())).thenReturn(List.of());

        mockMvc.perform(get("/users/2/expenses")
                        .with(user(new CustomUserDetails(admin))))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteSelf() throws Exception {
        when(userService.getUserById(2L)).thenReturn(Optional.of(user));
        doNothing().when(userService).deleteUser(2L);

        mockMvc.perform(delete("/users/2")
                        .with(user(new CustomUserDetails(user))))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteUserByAdmin() throws Exception {
        when(userService.getUserById(2L)).thenReturn(Optional.of(user));
        doNothing().when(userService).deleteUser(2L);

        mockMvc.perform(delete("/users/2")
                        .with(user(new CustomUserDetails(admin))))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteAdminSelfForbidden() throws Exception {
        when(userService.getUserById(1L)).thenReturn(Optional.of(admin));

        mockMvc.perform(delete("/users/1")
                        .with(user(new CustomUserDetails(admin))))
                .andExpect(status().isForbidden());
    }
}