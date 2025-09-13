package com.shybaiev.expense_tracker_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shybaiev.expense_tracker_backend.dto.BudgetCreateUpdateDto;
import com.shybaiev.expense_tracker_backend.dto.BudgetDto;
import com.shybaiev.expense_tracker_backend.entity.Budget;
import com.shybaiev.expense_tracker_backend.mapper.BudgetMapper;
import com.shybaiev.expense_tracker_backend.service.BudgetService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BudgetController.class)
@AutoConfigureMockMvc(addFilters = false)
class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BudgetService budgetService;

    @MockitoBean
    private BudgetMapper budgetMapper;

    @Test
    @WithMockUser(username = "testuser@email.com")
    void testAddBudget() throws Exception {
        BudgetCreateUpdateDto createDto = new BudgetCreateUpdateDto();
        createDto.setAmount(new BigDecimal("250.00"));
        createDto.setName("Monthly");
        createDto.setTimePeriod("MONTH");

        Budget saved = new Budget();
        saved.setId(42L);
        saved.setAmount(new BigDecimal("250.00"));
        saved.setName("Monthly");
        saved.setTimePeriod("MONTH");

        BudgetDto responseDto = new BudgetDto();
        responseDto.setId(42L);
        responseDto.setAmount(new BigDecimal("250.00"));
        responseDto.setName("Monthly");
        responseDto.setTimePeriod("MONTH");

        when(budgetService.createBudgetForUser(eq(createDto), eq("testuser@email.com")))
                .thenReturn(saved);

        when(budgetMapper.toDto(saved)).thenReturn(responseDto);

        mockMvc.perform(post("/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/budgets/42"))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.amount").value(250.00))
                .andExpect(jsonPath("$.name").value("Monthly"))
                .andExpect(jsonPath("$.timePeriod").value("MONTH"));
    }


    @Test
    @WithMockUser(username = "testuser")
    void testGetBudgetByIdFound() throws Exception {
        Budget entity = new Budget();
        entity.setId(5L);
        entity.setAmount(new BigDecimal("100.00"));
        entity.setName("Weekly");
        entity.setTimePeriod("WEEK");

        BudgetDto dto = new BudgetDto();
        dto.setId(5L);
        dto.setAmount(new BigDecimal("100.00"));
        dto.setName("Weekly");
        dto.setTimePeriod("WEEK");

        when(budgetService.getBudgetByIdForUser(5L,"testuser")).thenReturn(Optional.of(entity));
        when(budgetMapper.toDto(entity)).thenReturn(dto);

        mockMvc.perform(get("/budgets/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.name").value("Weekly"))
                .andExpect(jsonPath("$.timePeriod").value("WEEK"));
    }

    @Test
    @WithMockUser(username = "testuser@email.com")
    void testGetBudgetByIdNotFound() throws Exception {
        when(budgetService.getBudgetByIdForUser(404L, "testuser@email.com"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/budgets/404"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser@email.com")
    void testDeleteBudgetById() throws Exception {
        doNothing().when(budgetService).deleteBudgetForUser(7L, "testuser@email.com");

        mockMvc.perform(delete("/budgets/7"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "testuser@email.com")
    void testUpdateBudgetOk() throws Exception {
        BudgetCreateUpdateDto updateDto = new BudgetCreateUpdateDto();
        updateDto.setAmount(new BigDecimal("300.50"));
        updateDto.setName("Updated");
        updateDto.setTimePeriod("MONTH");

        Budget updated = new Budget();
        updated.setId(3L);
        updated.setAmount(new BigDecimal("300.50"));
        updated.setName("Updated");
        updated.setTimePeriod("MONTH");

        BudgetDto dto = new BudgetDto();
        dto.setId(3L);
        dto.setAmount(new BigDecimal("300.50"));
        dto.setName("Updated");
        dto.setTimePeriod("MONTH");

        when(budgetService.updateBudgetForUser(eq(3L), any(BudgetCreateUpdateDto.class), eq("testuser@email.com")))
                .thenReturn(updated);
        when(budgetMapper.toDto(updated)).thenReturn(dto);

        mockMvc.perform(patch("/budgets/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.amount").value(300.50))
                .andExpect(jsonPath("$.name").value("Updated"))
                .andExpect(jsonPath("$.timePeriod").value("MONTH"));
    }

    @Test
    @WithMockUser(username = "testuser@email.com")
    void testUpdateBudgetNotFound() throws Exception {
        BudgetCreateUpdateDto updateDto = new BudgetCreateUpdateDto();

        when(budgetService.updateBudgetForUser(eq(999L), any(BudgetCreateUpdateDto.class), eq("testuser@email.com")))
                .thenThrow(new EntityNotFoundException("not found"));

        mockMvc.perform(patch("/budgets/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllBudgets() throws Exception {
        Budget b1 = new Budget();
        b1.setId(1L);
        b1.setAmount(new BigDecimal("50.00"));
        b1.setName("B1");
        b1.setTimePeriod("WEEK");

        Budget b2 = new Budget();
        b2.setId(2L);
        b2.setAmount(new BigDecimal("200.00"));
        b2.setName("B2");
        b2.setTimePeriod("MONTH");

        BudgetDto d1 = new BudgetDto();
        d1.setId(1L);
        d1.setAmount(new BigDecimal("50.00"));
        d1.setName("B1");
        d1.setTimePeriod("WEEK");

        BudgetDto d2 = new BudgetDto();
        d2.setId(2L);
        d2.setAmount(new BigDecimal("200.00"));
        d2.setName("B2");
        d2.setTimePeriod("MONTH");

        when(budgetService.getAllBudgets()).thenReturn(List.of(b1, b2));
        when(budgetMapper.toDto(b1)).thenReturn(d1);
        when(budgetMapper.toDto(b2)).thenReturn(d2);

        mockMvc.perform(get("/budgets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].amount").value(50.00))
                .andExpect(jsonPath("$[0].name").value("B1"))
                .andExpect(jsonPath("$[0].timePeriod").value("WEEK"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].amount").value(200.00))
                .andExpect(jsonPath("$[1].name").value("B2"))
                .andExpect(jsonPath("$[1].timePeriod").value("MONTH"));
    }

    @Test
    @WithMockUser(username = "testuser@email.com")
    void testGetTotalExpensesForBudgetFound() throws Exception {
        Budget budget = new Budget();
        budget.setId(5L);

        when(budgetService.getBudgetByIdForUser(5L,"testuser@email.com")).thenReturn(Optional.of(budget));
        when(budgetService.getTotalExpensesForBudget(budget)).thenReturn(new BigDecimal("123.45"));

        mockMvc.perform(get("/budgets/budgets/5/expenses/total"))
                .andExpect(status().isOk())
                .andExpect(content().string("123.45"));
    }

    @Test
    @WithMockUser(username = "testuser@email.com")
    void testGetTotalExpensesForBudgetNotFound() throws Exception {
        when(budgetService.getBudgetByIdForUser(999L,"testuser@email.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/budgets/budgets/999/expenses/total"))
                .andExpect(status().isNotFound());
    }
}