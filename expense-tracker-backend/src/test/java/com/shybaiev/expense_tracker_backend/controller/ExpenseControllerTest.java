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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    @WithMockUser(username = "testuser@email.com")
    void testGetExpenseByIdFound() throws Exception {
        Expense entity = new Expense();
        entity.setId(5L);
        entity.setAmount(new BigDecimal("99.99"));
        entity.setDescription("Groceries");

        ExpenseDto dto = new ExpenseDto();
        dto.setId(5L);
        dto.setAmount(new BigDecimal("99.99"));
        dto.setDescription("Groceries");

        when(expenseService.getExpenseByIdForUser(5L, "testuser@email.com")).thenReturn(Optional.of(entity));
        when(expenseMapper.toDto(entity)).thenReturn(dto);

        mockMvc.perform(get("/expenses/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.amount").value(99.99))
                .andExpect(jsonPath("$.description").value("Groceries"));
    }

    @Test
    @WithMockUser(username = "testuser@email.com")
    void testGetExpenseByIdNotFound() throws Exception {
        when(expenseService.getExpenseByIdForUser(404L, "testuser@email.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/expenses/404"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser@email.com")
    void testDeleteExpenseById() throws Exception {
        doNothing().when(expenseService).deleteExpenseForUser(7L, "testuser@email.com");

        mockMvc.perform(delete("/expenses/7"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "testuser@email.com")
    void testDeleteExpenseById_NotFound() throws Exception {
        doThrow(new EntityNotFoundException("Expense not found")).when(expenseService).deleteExpenseForUser(404L, "testuser@email.com");

        mockMvc.perform(delete("/expenses/404"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser@email.com")
    void testDeleteExpenseById_AccessDenied() throws Exception {
        doThrow(new AccessDeniedException("Access denied")).when(expenseService).deleteExpenseForUser(7L, "testuser@email.com");

        mockMvc.perform(delete("/expenses/7"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testuser@email.com")
    void testUpdateExpenseOk() throws Exception {
        ExpenseCreateUpdateDto updateDto = new ExpenseCreateUpdateDto();
        updateDto.setAmount(new BigDecimal("15.50"));
        updateDto.setDescription("Updated");

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
        when(expenseService.updateExpenseForUser(eq(3L), any(ExpenseCreateUpdateDto.class), eq("testuser@email.com"))).thenReturn(updatedResult);
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
    @WithMockUser(username = "testuser@email.com")
    void testUpdateExpenseNotFound() throws Exception {
        ExpenseCreateUpdateDto updateDto = new ExpenseCreateUpdateDto();

        Expense mapped = new Expense();
        when(expenseMapper.toEntity(any(ExpenseCreateUpdateDto.class))).thenReturn(mapped);
        when(expenseService.updateExpenseForUser(eq(999L), any(ExpenseCreateUpdateDto.class), eq("testuser@email.com")))
                .thenThrow(new EntityNotFoundException("not found"));

        mockMvc.perform(patch("/expenses/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "testuser@email.com")
    void testUpdateExpenseAccessDenied() throws Exception {
        ExpenseCreateUpdateDto updateDto = new ExpenseCreateUpdateDto();

        Expense mapped = new Expense();
        when(expenseMapper.toEntity(any(ExpenseCreateUpdateDto.class))).thenReturn(mapped);
        when(expenseService.updateExpenseForUser(eq(3L), any(ExpenseCreateUpdateDto.class), eq("testuser@email.com")))
                .thenThrow(new AccessDeniedException("access denied"));

        mockMvc.perform(patch("/expenses/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testuser@email.com")
    void testGetAllExpenses() throws Exception {
        Expense e1 = new Expense();
        e1.setId(1L);
        e1.setAmount(new BigDecimal("50.00"));
        e1.setDescription("E1");
        e1.setCategory("Food");

        Expense e2 = new Expense();
        e2.setId(2L);
        e2.setAmount(new BigDecimal("200.00"));
        e2.setDescription("E2");
        e2.setCategory("Transport");

        ExpenseDto d1 = new ExpenseDto();
        d1.setId(1L);
        d1.setAmount(new BigDecimal("50.00"));
        d1.setDescription("E1");
        d1.setCategory("Food");

        ExpenseDto d2 = new ExpenseDto();
        d2.setId(2L);
        d2.setAmount(new BigDecimal("200.00"));
        d2.setDescription("E2");
        d2.setCategory("Transport");

        when(expenseService.getAllExpensesForUser("testuser@email.com")).thenReturn(List.of(e1, e2));
        when(expenseMapper.toDto(e1)).thenReturn(d1);
        when(expenseMapper.toDto(e2)).thenReturn(d2);

        mockMvc.perform(get("/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].amount").value(50.00))
                .andExpect(jsonPath("$[0].description").value("E1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].amount").value(200.00))
                .andExpect(jsonPath("$[1].description").value("E2"));
    }

    @Test
    @WithMockUser(username = "testuser@email.com")
    void testGetExpensesByCategory() throws Exception {
        Expense e1 = new Expense();
        e1.setId(1L);
        e1.setAmount(new BigDecimal("25.00"));
        e1.setDescription("Food expense");
        e1.setCategory("Food");

        ExpenseDto d1 = new ExpenseDto();
        d1.setId(1L);
        d1.setAmount(new BigDecimal("25.00"));
        d1.setDescription("Food expense");
        d1.setCategory("Food");

        when(expenseService.getExpensesByCategoryForUser("Food", "testuser@email.com")).thenReturn(List.of(e1));
        when(expenseMapper.toDto(e1)).thenReturn(d1);

        mockMvc.perform(get("/expenses/category/Food"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].amount").value(25.00))
                .andExpect(jsonPath("$[0].description").value("Food expense"))
                .andExpect(jsonPath("$[0].category").value("Food"));
    }

    @Test
    @WithMockUser(username = "testuser@email.com")
    void testGetExpensesByCategoryEmpty() throws Exception {
        when(expenseService.getExpensesByCategoryForUser("NonExistent", "testuser@email.com")).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/expenses/category/NonExistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}