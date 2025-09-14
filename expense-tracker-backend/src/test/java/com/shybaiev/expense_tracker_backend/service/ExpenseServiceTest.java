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

        expense = new Expense();
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
        //given
        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.of(user));
        when(expenseMapper.toEntity(expenseCreateUpdateDto)).thenReturn(expense);
        when(expenseRepository.save(any(Expense.class))).thenAnswer(i -> i.getArgument(0));
        //when
        Expense result = expenseService.createExpenseForUser(expenseCreateUpdateDto,"email@test.com");
        //then
        assertEquals(new BigDecimal("123.45"), result.getAmount());
        assertEquals(user, result.getUser());
        verify(expenseRepository).save(expense);
    }

    @Test
    void testGetExpenseById() {
        // given
        Long id = 1L;
        when(expenseRepository.findById(id)).thenReturn(Optional.of(expense));
        when(userRepository.findByEmail("email@test.com")).thenReturn(Optional.of(user));

        // when
        Optional<Expense> result = expenseService.getExpenseById(id);

        // then
        assertTrue(result.isPresent());
        assertEquals(expense, result.get());
        verify(expenseRepository).findById(id);
    }

    @Test
    void testUpdateExpenseSuccess() {
        // given
        Long id = 1L;
        Expense updated = new Expense();
        updated.setAmount(BigDecimal.valueOf(200));
        updated.setDescription("new desc");
        updated.setPlace("new place");
        updated.setCategory("new category");
        updated.setSource("new source");
        updated.setTimestamp(OffsetDateTime.now().minusDays(1));

        when(expenseRepository.findById(id)).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        Expense result = expenseService.updateExpense(id, updated);

        // then
        assertEquals(BigDecimal.valueOf(200), result.getAmount());
        assertEquals("new desc", result.getDescription());
        assertEquals("new place", result.getPlace());
        assertEquals("new category", result.getCategory());
        assertEquals("new source", result.getSource());
        assertEquals(updated.getTimestamp(), result.getTimestamp());

        verify(expenseRepository).findById(id);
        verify(expenseRepository).save(expense);
    }

    @Test
    void testUpdateExpenseNotFound() {
        // given
        Long missingId = 999L;
        when(expenseRepository.findById(missingId)).thenReturn(Optional.empty());

        // when + then
        assertThrows(EntityNotFoundException.class,
                () -> expenseService.updateExpense(missingId, new Expense()));

        verify(expenseRepository).findById(missingId);
        verify(expenseRepository, never()).save(any());
    }

    @Test
    void testGetAllExpenses() {
        // given
        Expense e1 = new Expense();
        e1.setUser(user);
        e1.setAmount(BigDecimal.TEN);
        e1.setTimestamp(OffsetDateTime.now());

        Expense e2 = new Expense();
        e2.setUser(user);
        e2.setAmount(BigDecimal.ONE);
        e2.setTimestamp(OffsetDateTime.now());

        when(expenseRepository.findAll()).thenReturn(List.of(e1, e2));

        // when
        List<Expense> result = expenseService.getAllExpenses();

        // then
        assertEquals(2, result.size());
        assertTrue(result.containsAll(List.of(e1, e2)));
        verify(expenseRepository).findAll();
    }

    @Test
    void testGetAllExpensesByUser() {
        // given
        Expense e1 = new Expense();
        e1.setUser(user);
        Expense e2 = new Expense();
        e2.setUser(user);

        when(expenseRepository.findAllByUser(user)).thenReturn(List.of(e1, e2));

        // when
        List<Expense> result = expenseService.getAllExpensesByUser(user);

        // then
        assertEquals(2, result.size());
        verify(expenseRepository).findAllByUser(user);
    }

    @Test
    void testGetAllExpensesByBudget() {
        // given
        Budget budget = new Budget();
        Expense e1 = new Expense();
        e1.setUser(user);
        Expense e2 = new Expense();
        e2.setUser(user);

        when(expenseRepository.findAllByBudget(budget)).thenReturn(List.of(e1, e2));

        // when
        List<Expense> result = expenseService.getAllExpensesByBudget(budget);

        // then
        assertEquals(2, result.size());
        verify(expenseRepository).findAllByBudget(budget);
    }

    @Test
    void testGetExpensesByCategoryAndUser() {
        // given
        String category = "food";
        Expense e1 = new Expense();
        e1.setCategory(category);
        e1.setUser(user);
        when(expenseRepository.findAllByCategoryAndUser(category, user)).thenReturn(List.of(e1));

        // when
        List<Expense> result = expenseService.getExpensesByCategoryAndUser(category, user);

        // then
        assertEquals(1, result.size());
        assertEquals(category, result.getFirst().getCategory());
        verify(expenseRepository).findAllByCategoryAndUser(category, user);
    }

    @Test
    void testGetExpensesByDateRangeAndUser() {
        // given
        OffsetDateTime from = OffsetDateTime.now().minusDays(7).withOffsetSameInstant(ZoneOffset.UTC);
        OffsetDateTime to = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
        Expense e1 = new Expense();
        e1.setUser(user);
        e1.setTimestamp(from.plusDays(1));
        when(expenseRepository.findAllByTimestampBetweenAndUser(from, to, user)).thenReturn(List.of(e1));

        // when
        List<Expense> result = expenseService.getExpensesByDateRangeAndUser(from, to, user);

        // then
        assertEquals(1, result.size());
        verify(expenseRepository).findAllByTimestampBetweenAndUser(from, to, user);
    }

    @Test
    void testDeleteExpenseSuccess() {
        // given
        Long id = 1L;
        when(expenseRepository.existsById(id)).thenReturn(true);

        // when
        expenseService.deleteExpense(id);

        // then
        verify(expenseRepository).existsById(id);
        verify(expenseRepository).deleteById(id);
    }

    @Test
    void testDeleteExpenseNotFound() {
        // given
        Long id = 999L;
        when(expenseRepository.existsById(id)).thenReturn(false);

        // when + then
        assertThrows(EntityNotFoundException.class, () -> expenseService.deleteExpense(id));
        verify(expenseRepository).existsById(id);
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

        when(expenseRepository.findAllByTimestampBetweenAndUser(any(OffsetDateTime.class), any(OffsetDateTime.class), eq(user)))
                .thenReturn(List.of(e1, e2, e3));

        // when
        BigDecimal total = expenseService.getTotalExpensesForUserInMonth(user, ym);

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
}
