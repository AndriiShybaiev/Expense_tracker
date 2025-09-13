package com.shybaiev.expense_tracker_backend.service;

import com.shybaiev.expense_tracker_backend.entity.Budget;
import com.shybaiev.expense_tracker_backend.entity.Expense;
import com.shybaiev.expense_tracker_backend.entity.Role;
import com.shybaiev.expense_tracker_backend.entity.User;
import com.shybaiev.expense_tracker_backend.repository.BudgetRepository;
import com.shybaiev.expense_tracker_backend.repository.ExpenseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ExpenseService expenseService;

    @InjectMocks
    private BudgetService budgetService;

    private User user;
    private Budget existingBudget;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("username");
        user.setEmail("email@test.com");
        user.setPasswordHash("pass");
        user.setRole(Role.USER);
        user.setEnabled(true);
        user.setBudgets(new ArrayList<>());

        existingBudget = new Budget();
        existingBudget.setId(1L);
        existingBudget.setAmount(new BigDecimal("100.00"));
        existingBudget.setName("Groceries");
        existingBudget.setDescription("Monthly groceries budget");
        existingBudget.setTimePeriod("MONTHLY");
        existingBudget.setStartDate(LocalDate.of(2024, 1, 1));
        existingBudget.setUser(user);

        user.getBudgets().add(existingBudget);
    }

    @Test
    void testCreateBudget() {
        // given
        when(budgetRepository.save(existingBudget)).thenReturn(existingBudget);

        // when
        Budget result = budgetService.createBudget(existingBudget);

        // then
        assertEquals(existingBudget, result);
        verify(budgetRepository).save(existingBudget);
    }

//    @Test
//    void testGetBudgetByIdForUser() {
//        // given
//        when(budgetRepository.findById(existingBudget.getId())).thenReturn(Optional.of(existingBudget));
//
//        // when
//        Optional<Budget> result = budgetService.getBudgetByIdForUser(existingBudget.getId());
//
//        // then
//        assertTrue(result.isPresent());
//        assertEquals(existingBudget, result.get());
//        verify(budgetRepository).findById(existingBudget.getId());
//    }

    @Test
    void testUpdateBudgetSuccess() {
        // given
        Budget updated = new Budget();
        updated.setAmount(new BigDecimal("250.50"));
        updated.setName("Updated Name");
        updated.setDescription("Updated Desc");
        updated.setTimePeriod("WEEKLY");
        updated.setStartDate(LocalDate.of(2024, 2, 1));

        when(budgetRepository.findById(1L)).thenReturn(Optional.of(existingBudget));
        when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        Budget result = budgetService.updateBudget(1L, updated);

        // then
        assertEquals(new BigDecimal("250.50"), result.getAmount());
        assertEquals("Updated Name", result.getName());
        assertEquals("Updated Desc", result.getDescription());
        assertEquals("WEEKLY", result.getTimePeriod());
        assertEquals(LocalDate.of(2024, 2, 1), result.getStartDate());

        verify(budgetRepository).findById(1L);
        verify(budgetRepository).save(existingBudget);
    }

    @Test
    void testUpdateBudgetNotFound() {
        // given
        when(budgetRepository.findById(999L)).thenReturn(Optional.empty());

        // when + then
        assertThrows(EntityNotFoundException.class, () -> budgetService.updateBudget(999L, new Budget()));

        verify(budgetRepository).findById(999L);
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    void testGetAllBudgets() {
        // given
        Budget b1 = new Budget();
        b1.setId(1L);
        b1.setAmount(new BigDecimal("10.00"));
        b1.setName("B1");
        b1.setTimePeriod("MONTHLY");
        b1.setStartDate(LocalDate.now());
        b1.setUser(user);

        Budget b2 = new Budget();
        b2.setId(2L);
        b2.setAmount(new BigDecimal("20.00"));
        b2.setName("B2");
        b2.setTimePeriod("MONTHLY");
        b2.setStartDate(LocalDate.now());
        b2.setUser(user);

        when(budgetRepository.findAll()).thenReturn(List.of(b1, b2));

        // when
        List<Budget> result = budgetService.getAllBudgets();

        // then
        assertEquals(2, result.size());
        assertTrue(result.containsAll(List.of(b1, b2)));
        verify(budgetRepository).findAll();
    }

    @Test
    void testGetAllBudgetsByUser() {
        // given
        Budget b1 = new Budget();
        b1.setUser(user);
        Budget b2 = new Budget();
        b2.setUser(user);

        when(budgetRepository.findAllByUser(user)).thenReturn(List.of(b1, b2));

        // when
        List<Budget> result = budgetService.getAllBudgetsByUser(user);

        // then
        assertEquals(2, result.size());
        verify(budgetRepository).findAllByUser(user);
    }

    @Test
    void testGetBudgetByExpenseSuccess() {
        // given
        Expense expense = new Expense();
        expense.setId(5L);
        when(budgetRepository.findByExpenses(expense)).thenReturn(Optional.of(existingBudget));

        // when
        Budget result = budgetService.getBudgetByExpense(expense);

        // then
        assertNotNull(result);
        assertEquals(existingBudget, result);
        verify(budgetRepository, atLeastOnce()).findByExpenses(expense);
    }

    @Test
    void testGetBudgetByExpenseNotFound() {
        // given
        Expense expense = new Expense();
        expense.setId(404L);
        when(budgetRepository.findByExpenses(expense)).thenReturn(Optional.empty());

        // when + then
        assertThrows(EntityNotFoundException.class, () -> budgetService.getBudgetByExpense(expense));
        verify(budgetRepository, atLeastOnce()).findByExpenses(expense);
    }

    @Test
    void testDeleteBudgetSuccess() {
        // given
        Long id = 1L;
        when(budgetRepository.existsById(id)).thenReturn(true);

        // when
        budgetService.deleteBudget(id);

        // then
        verify(budgetRepository).existsById(id);
        verify(budgetRepository).deleteById(id);
    }

    @Test
    void testDeleteBudgetNotFound() {
        // given
        Long id = 999L;
        when(budgetRepository.existsById(id)).thenReturn(false);

        // when + then
        assertThrows(EntityNotFoundException.class, () -> budgetService.deleteBudget(id));
        verify(budgetRepository).existsById(id);
        verify(budgetRepository, never()).deleteById(anyLong());
    }

    @Test
    void testGetTotalExpensesForBudget() {
        // given
        when(expenseRepository.getTotalExpensesByBudget(existingBudget))
                .thenReturn(new BigDecimal("33.33"));

        // when
        BigDecimal total = budgetService.getTotalExpensesForBudget(existingBudget);

        // then
        assertEquals(new BigDecimal("33.33"), total);
        verify(expenseRepository).getTotalExpensesByBudget(existingBudget);
    }

    @Test
    void testIsUserOverBudgetInMonth_NoBudgets() {
        // given
        User newUser = new User();
        newUser.setBudgets(new ArrayList<>());

        // when
        boolean result = budgetService.isUserOverBudgetInMonth(newUser, YearMonth.of(2024, 2));

        // then
        assertFalse(result);
        verifyNoInteractions(expenseService);
    }

    @Test
    void testIsUserOverBudgetInMonth_NotOver() {
        // given
        YearMonth ym = YearMonth.of(2024, 2);
        existingBudget.setAmount(new BigDecimal("100.00"));
        user.setBudgets(new ArrayList<>(List.of(existingBudget)));

        when(expenseService.getTotalExpensesForUserInMonth(user, ym)).thenReturn(new BigDecimal("99.99"));

        // when
        boolean result = budgetService.isUserOverBudgetInMonth(user, ym);

        // then
        assertFalse(result);
        verify(expenseService).getTotalExpensesForUserInMonth(user, ym);
    }

    @Test
    void testIsUserOverBudgetInMonth_Over() {
        // given
        YearMonth ym = YearMonth.of(2024, 2);
        existingBudget.setAmount(new BigDecimal("100.00"));
        user.setBudgets(new ArrayList<>(List.of(existingBudget)));

        when(expenseService.getTotalExpensesForUserInMonth(user, ym)).thenReturn(new BigDecimal("150.00"));

        // when
        boolean result = budgetService.isUserOverBudgetInMonth(user, ym);

        // then
        assertTrue(result);
        verify(expenseService).getTotalExpensesForUserInMonth(user, ym);
    }
}
