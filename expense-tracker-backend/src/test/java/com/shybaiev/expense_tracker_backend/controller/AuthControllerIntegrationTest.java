package com.shybaiev.expense_tracker_backend.controller;

import com.shybaiev.expense_tracker_backend.entity.User;
import com.shybaiev.expense_tracker_backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testRegisterUser() throws Exception {
        // language=JSON
        String registerJson = """
        {
          "username": "newuser",
          "email": "newuser@email.com",
          "password": "plainpassword"
        }
        """;

        // 1. Execute POST /auth/register
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@email.com"));

        // 2. Check if use in DB
        User saved = userRepository.findByEmail("newuser@email.com").orElseThrow();
        assertThat(saved.getUsername()).isEqualTo("newuser");

        // 3. Check password
        assertThat(passwordEncoder.matches("plainpassword", saved.getPasswordHash())).isTrue();
    }
}
