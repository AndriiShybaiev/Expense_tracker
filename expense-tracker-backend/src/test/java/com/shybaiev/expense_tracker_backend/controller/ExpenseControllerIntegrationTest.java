package com.shybaiev.expense_tracker_backend.controller;

import com.shybaiev.expense_tracker_backend.entity.Expense;
import com.shybaiev.expense_tracker_backend.entity.User;
import com.shybaiev.expense_tracker_backend.repository.ExpenseRepository;
import com.shybaiev.expense_tracker_backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class ExpenseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFullCrudCycle() throws Exception {

        User user = new User();
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setPasswordHash("hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd");
        user.setEnabled(true);
        user = userRepository.save(user);

        // language=JSON
        String createJson = """
        {
          "amount": 120.50,
          "description": "Internet Bill",
          "place": "ISP",
          "category": "Utilities",
          "source": "Card",
          "timestamp": "2025-09-02T15:00:00Z",
          "userId": 9223372036854775807
        }
        """;
        createJson = createJson.replace("9223372036854775807", String.valueOf(user.getId()));

        // 1. Create expense (POST)
        mockMvc.perform(post("/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.description").value("Internet Bill"));

        assertThat(expenseRepository.count()).isEqualTo(1);

        Expense saved = expenseRepository.findAll().getFirst();
        Long id = saved.getId();

        // 2. Get expense (GET)
        mockMvc.perform(get("/expenses/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.amount").value(120.50));

        // 3. Update expense (PATCH)
        // language=JSON
        String updateJson = """
            {
                "amount": 150.75,
                "description": "Updated Internet Bill",
                "place": "ISP",
                "category": "Utilities",
                "source": "Card",
                "timestamp": "2025-09-02T18:30:00Z"
            }
        """;

        mockMvc.perform(patch("/expenses/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(150.75))
                .andExpect(jsonPath("$.description").value("Updated Internet Bill"));

        // 4. Delete expense (DELETE)
        mockMvc.perform(delete("/expenses/" + id))
                .andExpect(status().isNoContent());

        assertThat(expenseRepository.existsById(id)).isFalse();

        // 5. Try to get deleted expense (GET → 404)
        mockMvc.perform(get("/expenses/" + id))
                .andExpect(status().isNotFound());
    }
}
