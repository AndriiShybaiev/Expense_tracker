package com.shybaiev.expense_tracker_backend.service;

import com.shybaiev.expense_tracker_backend.entity.Expense;
import com.shybaiev.expense_tracker_backend.entity.Role;
import com.shybaiev.expense_tracker_backend.entity.User;
import com.shybaiev.expense_tracker_backend.repository.ExpenseRepository;
import com.shybaiev.expense_tracker_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {
    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private ExpenseService expenseService;

    private User user;
    private Expense expense;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("username");
        user.setEmail("some@email.com");
        user.setPasswordHash("somePass");
        user.setRole(Role.USER);
        user.setEnabled(true);

        expense = new Expense();
        expense.setUser(user);
        expense.setAmount(BigDecimal.valueOf(123));
        expense.setCategory("category");
        expense.setDescription("description");
        expense.setPlace("place");
        expense.setSource("source");
        expense.setTimestamp(OffsetDateTime.now());
    }

    @Test
    void testCreateExpense() {
        //given
        //setUp
        //when
        when(expenseRepository.save(expense)).thenReturn(expense);
        //then
        assertEquals(expense, expenseService.createExpense(expense));
        verify(expenseRepository).save(expense);
    }
}
