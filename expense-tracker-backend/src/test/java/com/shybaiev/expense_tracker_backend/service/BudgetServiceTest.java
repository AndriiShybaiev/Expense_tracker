package com.shybaiev.expense_tracker_backend.service;

import com.shybaiev.expense_tracker_backend.dto.BudgetCreateUpdateDto;
import com.shybaiev.expense_tracker_backend.entity.Budget;
import com.shybaiev.expense_tracker_backend.entity.Expense;
import com.shybaiev.expense_tracker_backend.entity.Role;
import com.shybaiev.expense_tracker_backend.entity.User;
import com.shybaiev.expense_tracker_backend.mapper.BudgetMapper;
import com.shybaiev.expense_tracker_backend.repository.BudgetRepository;
import com.shybaiev.expense_tracker_backend.repository.ExpenseRepository;
import com.shybaiev.expense_tracker_backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

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
    private UserRepository userRepository;

    @Mock
    private ExpenseService expenseService;

    @Mock
    private BudgetMapper budgetMapper;

    @InjectMocks
    private BudgetService budgetService;

    private User user;
    private Budget existingBudget;
    private BudgetCreateUpdateDto budgetCreateUpdateDto;


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

        budgetCreateUpdateDto = new BudgetCreateUpdateDto();
        budgetCreateUpdateDto.setAmount(new BigDecimal("100.00"));
        budgetCreateUpdateDto.setName("Groceries");
        budgetCreateUpdateDto.setDescription("Monthly groceries budget");
        budgetCreateUpdateDto.setTimePeriod("MONTHLY");
        budgetCreateUpdateDto.setStartDate(LocalDate.of(2024, 1, 1));
        budgetCreateUpdateDto.setUserId(1L);


        user.getBudgets().add(existingBudget);
    }

    @Test
    @WithMockUser(username = "email@test.com")
    void testCreateBudgetForUser() {
        // given
        when(budgetRepository.save(existingBudget)).thenReturn(existingBudget);
        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.of(user));
        when(budgetMapper.toEntity(budgetCreateUpdateDto)).thenReturn(existingBudget);

        // when
        Budget result = budgetService.createBudgetForUser(budgetCreateUpdateDto,"email@test.com");

        // then
        assertEquals(existingBudget, result);
        verify(budgetRepository).save(existingBudget);
    }

    @Test
    @WithMockUser(username = "email@test.com")
    void testGetBudgetByIdForUser() {
        // given
        when(budgetRepository.findById(existingBudget.getId())).thenReturn(Optional.of(existingBudget));
        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.of(user));

        // when
        Optional<Budget> result = budgetService.getBudgetByIdForUser(existingBudget.getId(),"email@test.com");

        // then
        assertTrue(result.isPresent());
        assertEquals(existingBudget, result.get());
        verify(budgetRepository).findById(existingBudget.getId());
    }

    @Test
    @WithMockUser(username = "email@test.com")
    void testUpdateBudgetSuccess() {
        // given
        BudgetCreateUpdateDto budgetCreateUpdateDtoUpdated = new BudgetCreateUpdateDto();
        budgetCreateUpdateDtoUpdated.setAmount(new BigDecimal("250.50"));
        budgetCreateUpdateDtoUpdated.setName("Updated Name");
        budgetCreateUpdateDtoUpdated.setDescription("Updated Desc");
        budgetCreateUpdateDtoUpdated.setTimePeriod("WEEKLY");
        budgetCreateUpdateDtoUpdated.setStartDate(LocalDate.of(2024, 2, 1));

        when(budgetRepository.findById(1L)).thenReturn(Optional.of(existingBudget));
        when(budgetRepository.save(any(Budget.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.of(user));

        // when
        Budget result = budgetService.updateBudgetForUser(1L, budgetCreateUpdateDtoUpdated,"email@test.com");

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
    @WithMockUser(username = "email@test.com")
    void testUpdateBudgetNotFound() {
        // given
        when(budgetRepository.findById(999L)).thenReturn(Optional.empty());
        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.of(user));

        // when + then
        assertThrows(EntityNotFoundException.class, () -> budgetService.updateBudgetForUser(999L, budgetCreateUpdateDto,"email@test.com"));

        verify(budgetRepository).findById(999L);
        verify(budgetRepository, never()).save(any(Budget.class));
    }

    @Test
    @WithMockUser(username = "email@test.com")
    void testGetBudgetByExpenseForUser_Success() {
        // given
        Expense expense = new Expense();
        expense.setId(1L);

        User user = new User();
        user.setId(100L);
        user.setEmail("email@test.com");

        Budget budget = new Budget();
        budget.setId(10L);
        budget.setUser(user);

        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.of(user));
        when(budgetRepository.findByExpenses(expense)).thenReturn(Optional.of(budget));

        // when
        Budget result = budgetService.getBudgetByExpenseForUser(expense, "email@test.com");

        // then
        assertEquals(budget, result);
        verify(userRepository).findByEmail("email@test.com");
        verify(budgetRepository).findByExpenses(expense);
    }

    @Test
    @WithMockUser(username = "email@test.com")
    void testGetBudgetByExpenseForUser_UserNotFound() {
        Expense expense = new Expense();
        expense.setId(1L);

        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> budgetService.getBudgetByExpenseForUser(expense, "email@test.com"));

        verify(userRepository).findByEmail("email@test.com");
        verifyNoInteractions(budgetRepository);
    }

    @Test
    @WithMockUser(username = "email@test.com")
    void testGetBudgetByExpenseForUser_BudgetNotFound() {
        Expense expense = new Expense();
        expense.setId(1L);

        User user = new User();
        user.setId(100L);
        user.setEmail("email@test.com");

        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.of(user));
        when(budgetRepository.findByExpenses(expense)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> budgetService.getBudgetByExpenseForUser(expense, "email@test.com"));

        verify(userRepository).findByEmail("email@test.com");
        verify(budgetRepository).findByExpenses(expense);
    }

    @Test
    @WithMockUser(username = "email@test.com")
    void testGetBudgetByExpenseForUser_AccessDenied() {
        Expense expense = new Expense();
        expense.setId(1L);

        User currentUser = new User();
        currentUser.setId(100L);
        currentUser.setEmail("email@test.com");

        User anotherUser = new User();
        anotherUser.setId(200L);
        anotherUser.setEmail("other@test.com");

        Budget budget = new Budget();
        budget.setId(10L);
        budget.setUser(anotherUser);

        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.of(currentUser));
        when(budgetRepository.findByExpenses(expense)).thenReturn(Optional.of(budget));

        assertThrows(AccessDeniedException.class,
                () -> budgetService.getBudgetByExpenseForUser(expense, "email@test.com"));

        verify(userRepository).findByEmail("email@test.com");
        verify(budgetRepository).findByExpenses(expense);
    }

    @Test
    @WithMockUser(username = "email@test.com")
    void testDeleteBudgetForUser_Success() {
        // given
        User user = new User();
        user.setId(100L);
        user.setEmail("email@test.com");

        Budget budget = new Budget();
        budget.setId(10L);
        budget.setUser(user);

        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.of(user));
        when(budgetRepository.findById(10L)).thenReturn(Optional.of(budget));

        // when
        budgetService.deleteBudgetForUser(10L, "email@test.com");

        // then
        verify(budgetRepository).delete(budget);
    }

    @Test
    @WithMockUser(username = "email@test.com")
    void testDeleteBudgetForUser_UserNotFound() {
        // given
        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.empty());

        // when + then
        assertThrows(EntityNotFoundException.class,
                () -> budgetService.deleteBudgetForUser(10L, "email@test.com"));

        verifyNoInteractions(budgetRepository);
    }

    @Test
    @WithMockUser(username = "email@test.com")
    void testDeleteBudgetForUser_BudgetNotFound() {
        // given
        User user = new User();
        user.setId(100L);
        user.setEmail("email@test.com");

        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.of(user));
        when(budgetRepository.findById(10L)).thenReturn(Optional.empty());

        // when + then
        assertThrows(EntityNotFoundException.class,
                () -> budgetService.deleteBudgetForUser(10L, "email@test.com"));
    }

    @Test
    @WithMockUser(username = "email@test.com")
    void testDeleteBudgetForUser_AccessDenied() {
        // given
        User user = new User();
        user.setId(100L);
        user.setEmail("email@test.com");

        User anotherUser = new User();
        anotherUser.setId(200L);
        anotherUser.setEmail("other@test.com");

        Budget budget = new Budget();
        budget.setId(10L);
        budget.setUser(anotherUser);

        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.of(user));
        when(budgetRepository.findById(10L)).thenReturn(Optional.of(budget));

        // when + then
        assertThrows(AccessDeniedException.class,
                () -> budgetService.deleteBudgetForUser(10L, "email@test.com"));

        verify(budgetRepository, never()).delete(any(Budget.class));
    }

    @Test
    @WithMockUser(username = "email@test.com")
    void testGetTotalExpensesForBudgetForUser_Success() {
        // given
        User user = new User();
        user.setId(100L);
        user.setEmail("email@test.com");

        Budget budget = new Budget();
        budget.setId(10L);
        budget.setUser(user);

        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.of(user));
        when(expenseRepository.getTotalExpensesByBudget(budget)).thenReturn(new BigDecimal("123.45"));

        // when
        BigDecimal total = budgetService.getTotalExpensesForBudgetForUser(budget, "email@test.com");

        // then
        assertEquals(new BigDecimal("123.45"), total);
        verify(userRepository).findByEmail("email@test.com");
        verify(expenseRepository).getTotalExpensesByBudget(budget);
    }

    @Test
    @WithMockUser(username = "email@test.com")
    void testGetTotalExpensesForBudgetForUser_UserNotFound() {
        // given
        Budget budget = new Budget();
        budget.setId(10L);

        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.empty());

        // when + then
        assertThrows(EntityNotFoundException.class,
                () -> budgetService.getTotalExpensesForBudgetForUser(budget, "email@test.com"));

        verifyNoInteractions(expenseRepository);
    }

    @Test
    @WithMockUser(username = "email@test.com")
    void testGetTotalExpensesForBudgetForUser_AccessDenied() {
        // given
        User user = new User();
        user.setId(100L);
        user.setEmail("email@test.com");

        User anotherUser = new User();
        anotherUser.setId(200L);
        anotherUser.setEmail("other@test.com");

        Budget budget = new Budget();
        budget.setId(10L);
        budget.setUser(anotherUser);

        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.of(user));

        // when + then
        assertThrows(AccessDeniedException.class,
                () -> budgetService.getTotalExpensesForBudgetForUser(budget, "email@test.com"));

        verify(expenseRepository, never()).getTotalExpensesByBudget(any(Budget.class));
    }

    @Test
    @WithMockUser(username = "email@test.com")
    void testIsUserOverBudgetInMonthForUser_NoBudgets() {
        // given
        User user = new User();
        user.setEmail("email@test.com");
        user.setBudgets(new ArrayList<>());

        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.of(user));

        // when
        boolean result = budgetService.isUserOverBudgetInMonthForUser("email@test.com", YearMonth.of(2024, 2));

        // then
        assertFalse(result);
        verifyNoInteractions(expenseService);
    }

    @Test
    @WithMockUser(username = "email@test.com")
    void testIsUserOverBudgetInMonthForUser_NotOver() {
        // given
        User user = new User();
        user.setId(1L);
        user.setEmail("email@test.com");

        Budget budget = new Budget();
        budget.setAmount(new BigDecimal("100.00"));
        user.setBudgets(new ArrayList<>(List.of(budget)));

        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.of(user));
        when(expenseService.getTotalExpensesForUserInMonth(user, YearMonth.of(2024, 2)))
                .thenReturn(new BigDecimal("99.99"));

        // when
        boolean result = budgetService.isUserOverBudgetInMonthForUser("email@test.com", YearMonth.of(2024, 2));

        // then
        assertFalse(result);
        verify(expenseService).getTotalExpensesForUserInMonth(user, YearMonth.of(2024, 2));
    }

    @Test
    @WithMockUser(username = "email@test.com")
    void testIsUserOverBudgetInMonthForUser_Over() {
        // given
        User user = new User();
        user.setId(1L);
        user.setEmail("email@test.com");

        Budget budget = new Budget();
        budget.setAmount(new BigDecimal("100.00"));
        user.setBudgets(new ArrayList<>(List.of(budget)));

        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.of(user));
        when(expenseService.getTotalExpensesForUserInMonth(user, YearMonth.of(2024, 2)))
                .thenReturn(new BigDecimal("150.00"));

        // when
        boolean result = budgetService.isUserOverBudgetInMonthForUser("email@test.com", YearMonth.of(2024, 2));

        // then
        assertTrue(result);
        verify(expenseService).getTotalExpensesForUserInMonth(user, YearMonth.of(2024, 2));
    }

    @Test
    @WithMockUser(username = "email@test.com")
    void testIsUserOverBudgetInMonthForUser_UserNotFound() {
        // given
        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.empty());

        // when + then
        assertThrows(EntityNotFoundException.class,
                () -> budgetService.isUserOverBudgetInMonthForUser("email@test.com", YearMonth.of(2024, 2)));

        verifyNoInteractions(expenseService);
    }

}
