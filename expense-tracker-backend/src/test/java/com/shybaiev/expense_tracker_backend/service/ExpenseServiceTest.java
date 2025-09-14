package com.shybaiev.expense_tracker_backend.service;

import com.shybaiev.expense_tracker_backend.dto.ExpenseCreateUpdateDto;
import com.shybaiev.expense_tracker_backend.entity.Expense;
import com.shybaiev.expense_tracker_backend.entity.Role;
import com.shybaiev.expense_tracker_backend.entity.User;
import com.shybaiev.expense_tracker_backend.entity.Budget;
import com.shybaiev.expense_tracker_backend.mapper.ExpenseMapper;
import com.shybaiev.expense_tracker_backend.repository.ExpenseRepository;
import com.shybaiev.expense_tracker_backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {
    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ExpenseMapper expenseMapper;
    @InjectMocks
    private ExpenseService expenseService;

    private User user;
    private User otherUser;
    private Expense expense;
    private ExpenseCreateUpdateDto expenseCreateUpdateDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("username");
        user.setEmail("email@test.com");
        user.setPasswordHash("somePass");
        user.setRole(Role.USER);
        user.setEnabled(true);

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("other");
        otherUser.setEmail("other@test.com");
        otherUser.setPasswordHash("pass");
        otherUser.setRole(Role.USER);
        otherUser.setEnabled(true);

        expense = new Expense();
        expense.setId(1L);
        expense.setUser(user);
        expense.setAmount(BigDecimal.valueOf(123.45));
        expense.setCategory("category");
        expense.setDescription("description");
        expense.setPlace("place");
        expense.setSource("source");
        expense.setTimestamp(OffsetDateTime.now());

        expenseCreateUpdateDto = new ExpenseCreateUpdateDto();
        expenseCreateUpdateDto.setAmount(BigDecimal.valueOf(123.45));
        expenseCreateUpdateDto.setCategory("category");
        expenseCreateUpdateDto.setDescription("description");
        expenseCreateUpdateDto.setPlace("place");
        expenseCreateUpdateDto.setSource("source");
        expenseCreateUpdateDto.setTimestamp(OffsetDateTime.now());
    }

    @Test
    void testCreateExpenseForUser() {
        // given
        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.of(user));
        when(expenseMapper.toEntity(expenseCreateUpdateDto)).thenReturn(expense);
        when(expenseRepository.save(any(Expense.class))).thenAnswer(i -> i.getArgument(0));

        // when
        Expense result = expenseService.createExpenseForUser(expenseCreateUpdateDto, "email@test.com");

        // then
        assertEquals(new BigDecimal("123.45"), result.getAmount());
        assertEquals(user, result.getUser());
        verify(expenseRepository).save(expense);
    }

    @Test
    void testCreateExpenseForUser_UserNotFound() {
        // given
        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // when + then
        assertThrows(IllegalArgumentException.class,
                () -> expenseService.createExpenseForUser(expenseCreateUpdateDto, "nonexistent@test.com"));

        verify(userRepository).findByEmail("nonexistent@test.com");
        verifyNoInteractions(expenseRepository);
    }

    @Test
    void testGetExpenseByIdForUser() {
        // given
        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.of(user));
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));

        // when
        Optional<Expense> result = expenseService.getExpenseByIdForUser(1L, "email@test.com");

        // then
        assertTrue(result.isPresent());
        assertEquals(expense, result.get());
        verify(expenseRepository).findById(1L);
    }

    @Test
    void testGetExpenseByIdForUser_ExpenseNotFound() {
        // given
        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.of(user));
        when(expenseRepository.findById(404L)).thenReturn(Optional.empty());

        // when
        Optional<Expense> result = expenseService.getExpenseByIdForUser(404L, "email@test.com");

        // then
        assertFalse(result.isPresent());
        verify(expenseRepository).findById(404L);
    }

    @Test
    void testGetExpenseByIdForUser_UserNotFound() {
        // given
        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // when + then
        assertThrows(EntityNotFoundException.class,
                () -> expenseService.getExpenseByIdForUser(1L, "nonexistent@test.com"));

        verify(userRepository).findByEmail("nonexistent@test.com");
        verifyNoInteractions(expenseRepository);
    }

    @Test
    void testGetExpenseByIdForUser_ExpenseNotBelongsToUser() {
        // given
        expense.setUser(otherUser);
        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.of(user));
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));

        // when
        Optional<Expense> result = expenseService.getExpenseByIdForUser(1L, "email@test.com");

        // then
        assertFalse(result.isPresent());
        verify(expenseRepository).findById(1L);
    }

    @Test
    void testUpdateExpenseForUser_Success() {
        // given
        ExpenseCreateUpdateDto updated = new ExpenseCreateUpdateDto();
        updated.setAmount(BigDecimal.valueOf(200));
        updated.setDescription("new desc");
        updated.setPlace("new place");
        updated.setCategory("new category");
        updated.setSource("new source");
        updated.setTimestamp(OffsetDateTime.now().minusDays(1));

        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.of(user));
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        Expense result = expenseService.updateExpenseForUser(1L, updated, "email@test.com");

        // then
        assertEquals(BigDecimal.valueOf(200), result.getAmount());
        assertEquals("new desc", result.getDescription());
        assertEquals("new place", result.getPlace());
        assertEquals("new category", result.getCategory());
        assertEquals("new source", result.getSource());
        assertEquals(updated.getTimestamp(), result.getTimestamp());

        verify(expenseRepository).findById(1L);
        verify(expenseRepository).save(expense);
    }

    @Test
    void testUpdateExpenseForUser_ExpenseNotFound() {
        // given
        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.of(user));
        when(expenseRepository.findById(999L)).thenReturn(Optional.empty());

        // when + then
        assertThrows(EntityNotFoundException.class,
                () -> expenseService.updateExpenseForUser(999L, new ExpenseCreateUpdateDto(), "email@test.com"));

        verify(expenseRepository).findById(999L);
        verify(expenseRepository, never()).save(any());
    }

    @Test
    void testUpdateExpenseForUser_UserNotFound() {
        // given
        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // when + then
        assertThrows(EntityNotFoundException.class,
                () -> expenseService.updateExpenseForUser(1L, new ExpenseCreateUpdateDto(), "nonexistent@test.com"));

        verify(userRepository).findByEmail("nonexistent@test.com");
        verifyNoInteractions(expenseRepository);
    }

    @Test
    void testUpdateExpenseForUser_AccessDenied() {
        // given
        expense.setUser(otherUser);
        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.of(user));
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));

        // when + then
        assertThrows(AccessDeniedException.class,
                () -> expenseService.updateExpenseForUser(1L, new ExpenseCreateUpdateDto(), "email@test.com"));

        verify(expenseRepository).findById(1L);
        verify(expenseRepository, never()).save(any());
    }

    @Test
    void testGetAllExpensesForUser() {
        // given
        Expense e1 = new Expense();
        e1.setUser(user);
        e1.setAmount(BigDecimal.TEN);
        e1.setTimestamp(OffsetDateTime.now());

        Expense e2 = new Expense();
        e2.setUser(user);
        e2.setAmount(BigDecimal.ONE);
        e2.setTimestamp(OffsetDateTime.now());

        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.of(user));
        when(expenseRepository.findAllByUser(user)).thenReturn(List.of(e1, e2));

        // when
        List<Expense> result = expenseService.getAllExpensesForUser("email@test.com");

        // then
        assertEquals(2, result.size());
        assertTrue(result.containsAll(List.of(e1, e2)));
        verify(expenseRepository).findAllByUser(user);
    }

    @Test
    void testGetAllExpensesForUser_UserNotFound() {
        // given
        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // when + then
        assertThrows(EntityNotFoundException.class,
                () -> expenseService.getAllExpensesForUser("nonexistent@test.com"));

        verify(userRepository).findByEmail("nonexistent@test.com");
        verifyNoInteractions(expenseRepository);
    }

    @Test
    void testGetExpensesByCategoryForUser() {
        // given
        String category = "food";
        Expense e1 = new Expense();
        e1.setCategory(category);
        e1.setUser(user);

        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.of(user));
        when(expenseRepository.findAllByCategoryAndUser(category, user)).thenReturn(List.of(e1));

        // when
        List<Expense> result = expenseService.getExpensesByCategoryForUser(category, "email@test.com");

        // then
        assertEquals(1, result.size());
        assertEquals(category, result.getFirst().getCategory());
        verify(expenseRepository).findAllByCategoryAndUser(category, user);
    }

    @Test
    void testGetExpensesByDateRangeForUser() {
        // given
        OffsetDateTime from = OffsetDateTime.now().minusDays(7).withOffsetSameInstant(ZoneOffset.UTC);
        OffsetDateTime to = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
        Expense e1 = new Expense();
        e1.setUser(user);
        e1.setTimestamp(from.plusDays(1));

        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.of(user));
        when(expenseRepository.findAllByTimestampBetweenAndUser(from, to, user)).thenReturn(List.of(e1));

        // when
        List<Expense> result = expenseService.getExpensesByDateRangeForUser(from, to, "email@test.com");

        // then
        assertEquals(1, result.size());
        verify(expenseRepository).findAllByTimestampBetweenAndUser(from, to, user);
    }

    @Test
    void testDeleteExpenseForUser_Success() {
        // given
        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.of(user));
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));

        // when
        expenseService.deleteExpenseForUser(1L, "email@test.com");

        // then
        verify(expenseRepository).findById(1L);
        verify(expenseRepository).deleteById(1L);
    }

    @Test
    void testDeleteExpenseForUser_ExpenseNotFound() {
        // given
        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.of(user));
        when(expenseRepository.findById(999L)).thenReturn(Optional.empty());

        // when + then
        assertThrows(EntityNotFoundException.class,
                () -> expenseService.deleteExpenseForUser(999L, "email@test.com"));

        verify(expenseRepository).findById(999L);
        verify(expenseRepository, never()).deleteById(anyLong());
    }

    @Test
    void testDeleteExpenseForUser_UserNotFound() {
        // given
        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // when + then
        assertThrows(EntityNotFoundException.class,
                () -> expenseService.deleteExpenseForUser(1L, "nonexistent@test.com"));

        verify(userRepository).findByEmail("nonexistent@test.com");
        verifyNoInteractions(expenseRepository);
    }

    @Test
    void testDeleteExpenseForUser_AccessDenied() {
        // given
        expense.setUser(otherUser);
        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.of(user));
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));

        // when + then
        assertThrows(AccessDeniedException.class,
                () -> expenseService.deleteExpenseForUser(1L, "email@test.com"));

        verify(expenseRepository).findById(1L);
        verify(expenseRepository, never()).deleteById(anyLong());
    }

    @Test
    void testGetTotalExpensesForUserInMonth() {
        // given
        YearMonth ym = YearMonth.of(2024, 2);
        Expense e1 = new Expense();
        e1.setAmount(new BigDecimal("10.50"));
        e1.setUser(user);

        Expense e2 = new Expense();
        e2.setAmount(new BigDecimal("5.25"));
        e2.setUser(user);

        Expense e3 = new Expense(); // null amount should be skipped
        e3.setAmount(null);
        e3.setUser(user);

        ArgumentCaptor<OffsetDateTime> startCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);
        ArgumentCaptor<OffsetDateTime> endCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);

        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.of(user));
        when(expenseRepository.findAllByTimestampBetweenAndUser(any(OffsetDateTime.class), any(OffsetDateTime.class), eq(user)))
                .thenReturn(List.of(e1, e2, e3));

        // when
        BigDecimal total = expenseService.getTotalExpensesForUserInMonth("email@test.com", ym);

        // then
        assertEquals(new BigDecimal("15.75"), total);

        verify(expenseRepository).findAllByTimestampBetweenAndUser(startCaptor.capture(), endCaptor.capture(), eq(user));

        OffsetDateTime start = startCaptor.getValue();
        OffsetDateTime end = endCaptor.getValue();

        assertEquals(1, start.getDayOfMonth());
        assertEquals(0, start.getHour());
        assertEquals(0, start.getMinute());
        assertEquals(0, start.getSecond());
        assertEquals(ZoneOffset.UTC, start.getOffset());

        assertEquals(ym.atEndOfMonth().getDayOfMonth(), end.getDayOfMonth());
        assertEquals(23, end.getHour());
        assertEquals(59, end.getMinute());
        assertEquals(59, end.getSecond());
        assertEquals(ZoneOffset.UTC, end.getOffset());
    }

    @Test
    void testGetTotalExpensesForUserInMonth_UserNotFound() {
        // given
        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // when + then
        assertThrows(EntityNotFoundException.class,
                () -> expenseService.getTotalExpensesForUserInMonth("nonexistent@test.com", YearMonth.of(2024, 1)));

        verify(userRepository).findByEmail("nonexistent@test.com");
        verifyNoInteractions(expenseRepository);
    }

    @Test
    void testGetAllExpensesByBudgetForUser_Success() {
        // given
        Budget budget = new Budget();
        budget.setUser(user);
        Expense e1 = new Expense();
        e1.setUser(user);
        Expense e2 = new Expense();
        e2.setUser(user);

        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.of(user));
        when(expenseRepository.findAllByBudget(budget)).thenReturn(List.of(e1, e2));

        // when
        List<Expense> result = expenseService.getAllExpensesByBudgetForUser(budget, "email@test.com");

        // then
        assertEquals(2, result.size());
        verify(expenseRepository).findAllByBudget(budget);
    }

    @Test
    void testGetAllExpensesByBudgetForUser_AccessDenied() {
        // given
        Budget budget = new Budget();
        budget.setUser(otherUser);

        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.of(user));

        // when + then
        assertThrows(AccessDeniedException.class,
                () -> expenseService.getAllExpensesByBudgetForUser(budget, "email@test.com"));

        verify(userRepository).findByEmail("email@test.com");
        verifyNoInteractions(expenseRepository);
    }
}