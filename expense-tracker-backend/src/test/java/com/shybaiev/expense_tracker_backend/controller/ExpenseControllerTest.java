package com.shybaiev.expense_tracker_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shybaiev.expense_tracker_backend.dto.ExpenseCreateUpdateDto;
import com.shybaiev.expense_tracker_backend.dto.ExpenseDto;
import com.shybaiev.expense_tracker_backend.entity.Expense;
import com.shybaiev.expense_tracker_backend.mapper.ExpenseMapper;
import com.shybaiev.expense_tracker_backend.service.ExpenseService;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseController.class)
@AutoConfigureMockMvc(addFilters = false)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ExpenseService expenseService;

    @MockitoBean
    private ExpenseMapper expenseMapper;

    @Test
    @WithMockUser(username = "testuser@email.com")
    void testAddExpense() throws Exception {
        ExpenseCreateUpdateDto createDto = new ExpenseCreateUpdateDto();
        createDto.setAmount(new BigDecimal("12.34"));
        createDto.setDescription("Lunch");

        Expense entityToSave = new Expense();
        entityToSave.setAmount(new BigDecimal("12.34"));
        entityToSave.setDescription("Lunch");

        Expense saved = new Expense();
        saved.setId(10L);
        saved.setAmount(new BigDecimal("12.34"));
        saved.setDescription("Lunch");

        ExpenseDto responseDto = new ExpenseDto();
        responseDto.setId(10L);
        responseDto.setAmount(new BigDecimal("12.34"));
        responseDto.setDescription("Lunch");

        when(expenseService.createExpenseForUser(eq(createDto), eq("testuser@email.com"))).thenReturn(saved);

        when(expenseMapper.toDto(saved)).thenReturn(responseDto);

        mockMvc.perform(post("/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/expenses/10"))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.amount").value(12.34))
                .andExpect(jsonPath("$.description").value("Lunch"));
    }

    @Test
    void testGetExpenseByIdFound() throws Exception {
        Expense entity = new Expense();
        entity.setId(5L);
        entity.setAmount(new BigDecimal("99.99"));
        entity.setDescription("Groceries");

        ExpenseDto dto = new ExpenseDto();
        dto.setId(5L);
        dto.setAmount(new BigDecimal("99.99"));
        dto.setDescription("Groceries");

        when(expenseService.getExpenseById(5L)).thenReturn(Optional.of(entity));
        when(expenseMapper.toDto(entity)).thenReturn(dto);

        mockMvc.perform(get("/expenses/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.amount").value(99.99))
                .andExpect(jsonPath("$.description").value("Groceries"));
    }

    @Test
    void testGetExpenseByIdNotFound() throws Exception {
        when(expenseService.getExpenseById(404L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/expenses/404"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteExpenseById() throws Exception {
        // By default, our mock does nothing (no exception), so controller returns 204
        mockMvc.perform(delete("/expenses/7"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testUpdateExpenseOk() throws Exception {
        ExpenseCreateUpdateDto updateDto = new ExpenseCreateUpdateDto();

        Expense updatedInput = new Expense();
        updatedInput.setAmount(new BigDecimal("15.50"));
        updatedInput.setDescription("Updated");

        Expense updatedResult = new Expense();
        updatedResult.setId(3L);
        updatedResult.setAmount(new BigDecimal("15.50"));
        updatedResult.setDescription("Updated");

        ExpenseDto dto = new ExpenseDto();
        dto.setId(3L);
        dto.setAmount(new BigDecimal("15.50"));
        dto.setDescription("Updated");

        when(expenseMapper.toEntity(any(ExpenseCreateUpdateDto.class))).thenReturn(updatedInput);
        when(expenseService.updateExpense(3L, updatedInput)).thenReturn(updatedResult);
        when(expenseMapper.toDto(updatedResult)).thenReturn(dto);

        mockMvc.perform(patch("/expenses/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.amount").value(15.50))
                .andExpect(jsonPath("$.description").value("Updated"));
    }

    @Test
    void testUpdateExpenseNotFound() throws Exception {
        ExpenseCreateUpdateDto updateDto = new ExpenseCreateUpdateDto();

        Expense mapped = new Expense();
        when(expenseMapper.toEntity(any(ExpenseCreateUpdateDto.class))).thenReturn(mapped);
        when(expenseService.updateExpense(999L, mapped)).thenThrow(new EntityNotFoundException("not found"));

        mockMvc.perform(patch("/expenses/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }
}