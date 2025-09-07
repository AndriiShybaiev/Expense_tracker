package com.shybaiev.expense_tracker_backend.controller;

import com.shybaiev.expense_tracker_backend.entity.Budget;
import com.shybaiev.expense_tracker_backend.entity.User;
import com.shybaiev.expense_tracker_backend.repository.BudgetRepository;
import com.shybaiev.expense_tracker_backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class BudgetControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFullCrudCycleBudget() throws Exception {

        User user = new User();
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setPasswordHash("hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd");
        user.setEnabled(true);
        user = userRepository.save(user);

        // language=JSON
        String createJson = """
        {
          "amount": 1020.50,
          "name": "My monthly budget",
          "description": "some descripton",
          "timePeriod": "30",
          "startDate": "2025-09-01",
          "userId": 9223372036854775807
        }
        """;
        createJson = createJson.replace("9223372036854775807", String.valueOf(user.getId()));

        // 1. Create budget (POST)
        mockMvc.perform(post("/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.description").value("some descripton"));

        assertThat(budgetRepository.count()).isEqualTo(1);

        Budget saved = budgetRepository.findAll().getFirst();
        Long id = saved.getId();

        // 2. Get budget (GET)
        mockMvc.perform(get("/budgets/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.amount").value(1020.50));

        // 3. Update budget (PATCH)
        // language=JSON
        String updateJson = """
        {
          "amount": 1520.50,
          "name": "My monthly budget",
          "description": "some updated descripton",
          "timePeriod": "30",
          "startDate": "2025-09-01",
          "userId": 9223372036854775807
        }
        """;

        mockMvc.perform(patch("/budgets/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(1520.50))
                .andExpect(jsonPath("$.description").value("some updated descripton"));

        // 4. Delete budget (DELETE)
        mockMvc.perform(delete("/budgets/" + id))
                .andExpect(status().isNoContent());

        assertThat(budgetRepository.existsById(id)).isFalse();

        // 5. Try to get deleted budget (GET → 404)
        mockMvc.perform(get("/budget/" + id))
                .andExpect(status().isNotFound());
    }
}
