package com.shybaiev.expense_tracker_backend.controller;

import com.shybaiev.expense_tracker_backend.entity.Role;
import com.shybaiev.expense_tracker_backend.entity.User;
import com.shybaiev.expense_tracker_backend.repository.UserRepository;
import com.shybaiev.expense_tracker_backend.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFullCrudCycleUser() throws Exception {


        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@test.com");
        admin.setPasswordHash("test_password_123456test_password_123456test_password_123456"); // хэш
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        userRepository.save(admin);

        // language=JSON
        String createJson = """
        {
          "username": "Itestuser",
          "email": "Itestemail@email.com",
          "password": "test_password_123456test_password_123456test_password_123456",
          "role": "USER"
        }
        """;

        // 1. Create user (POST) — as admin
        mockMvc.perform(post("/users")
                        .with(user(new CustomUserDetails(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.email").value("Itestemail@email.com"));

        assertThat(userRepository.count()).isEqualTo(2); // admin + созданный

        User saved = userRepository.findAll()
                .stream()
                .filter(u -> u.getEmail().equals("Itestemail@email.com"))
                .findFirst()
                .orElseThrow();
        Long id = saved.getId();

        // 2. Get user (GET)
        mockMvc.perform(get("/users/" + id)
                        .with(user(new CustomUserDetails(admin))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.username").value("Itestuser"));

        // 3. Update user (PATCH)
        // language=JSON
        String updateJson = """
         {
          "username": "Itestuser",
          "email": "new@email.com",
          "password": "test_password_123456test_password_123456test_password_123456",
          "role": "ADMIN"
        }
        """;

        mockMvc.perform(patch("/users/" + id)
                        .with(user(new CustomUserDetails(admin)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@email.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        assertThat(userRepository.findById(id).get().getEmail()).isEqualTo("new@email.com");

        // 4. Delete user (DELETE)
        mockMvc.perform(delete("/users/" + id)
                        .with(user(new CustomUserDetails(admin))))
                .andExpect(status().isNoContent());

        assertThat(userRepository.existsById(id)).isFalse();

        // 5. Try to get deleted user (GET → 404)
        mockMvc.perform(get("/users/" + id)
                        .with(user(new CustomUserDetails(admin))))
                .andExpect(status().isNotFound());
    }
}

