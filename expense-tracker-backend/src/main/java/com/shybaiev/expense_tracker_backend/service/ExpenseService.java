package com.shybaiev.expense_tracker_backend.service;

import com.shybaiev.expense_tracker_backend.dto.ExpenseCreateUpdateDto;
import com.shybaiev.expense_tracker_backend.entity.Budget;
import com.shybaiev.expense_tracker_backend.entity.Expense;
import com.shybaiev.expense_tracker_backend.entity.User;
import com.shybaiev.expense_tracker_backend.mapper.ExpenseMapper;
import com.shybaiev.expense_tracker_backend.repository.ExpenseRepository;
import com.shybaiev.expense_tracker_backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final ExpenseMapper expenseMapper;

    public Expense createExpenseForUser(ExpenseCreateUpdateDto expenseCreateUpdateDto, String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Expense expense = expenseMapper.toEntity(expenseCreateUpdateDto);
        expense.setUser(user);
        return expenseRepository.save(expense);
    }

    public Optional<Expense> getExpenseByIdForUser(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + email));

        Optional<Expense> maybeExpense = expenseRepository.findById(id);
        if (maybeExpense.isEmpty()) {
            return Optional.empty();
        }

        Expense expense = maybeExpense.get();
        if (expense.getUser() == null || !Objects.equals(expense.getUser().getId(), user.getId())) {
            return Optional.empty(); // Expense doesn't belong to user
        }

        return Optional.of(expense);
    }

    public Expense updateExpenseForUser(Long existingExpenseId, ExpenseCreateUpdateDto dto, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Expense existingExpense = expenseRepository.findById(existingExpenseId)
                .orElseThrow(() -> new EntityNotFoundException("Expense with id " + existingExpenseId + " not found"));

        if (existingExpense.getUser() == null || !Objects.equals(existingExpense.getUser().getId(), user.getId())) {
            throw new AccessDeniedException("You are not allowed to update this expense");
        }

        if (dto.getAmount() != null) {
            existingExpense.setAmount(dto.getAmount());
        }
        if (dto.getDescription() != null) {
            existingExpense.setDescription(dto.getDescription());
        }
        if (dto.getPlace() != null) {
            existingExpense.setPlace(dto.getPlace());
        }
        if (dto.getCategory() != null) {
            existingExpense.setCategory(dto.getCategory());
        }
        if (dto.getSource() != null) {
            existingExpense.setSource(dto.getSource());
        }
        if (dto.getTimestamp() != null) {
            existingExpense.setTimestamp(dto.getTimestamp());
        }

        return expenseRepository.save(existingExpense);
    }


    public List<Expense> getAllExpensesForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + email));

        return expenseRepository.findAllByUser(user);
    }

    public List<Expense> getAllExpensesByBudgetForUser(Budget budget, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + email));

        if (budget.getUser() == null || !Objects.equals(budget.getUser().getId(), user.getId())) {
            throw new AccessDeniedException("You are not allowed to access this budget's expenses");
        }

        return expenseRepository.findAllByBudget(budget);
    }

    public List<Expense> getExpensesByCategoryForUser(String category, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + email));

        return expenseRepository.findAllByCategoryAndUser(category, user);
    }

    public List<Expense> getExpensesByDateRangeForUser(OffsetDateTime from, OffsetDateTime to, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + email));

        return expenseRepository.findAllByTimestampBetweenAndUser(from, to, user);
    }

    public void deleteExpenseForUser(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + email));

        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Expense with id " + id + " not found"));

        if (expense.getUser() == null || !Objects.equals(expense.getUser().getId(), user.getId())) {
            throw new AccessDeniedException("You are not allowed to delete this expense");
        }

        expenseRepository.deleteById(id);
    }

    public BigDecimal getTotalExpensesForUserInMonth(String email, YearMonth yearMonth) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + email));

        OffsetDateTime start = yearMonth.atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59).atOffset(ZoneOffset.UTC);

        List<Expense> expenses = expenseRepository.findAllByTimestampBetweenAndUser(start, end, user);

        BigDecimal total = BigDecimal.ZERO;
        for (Expense expense : expenses) {
            if (expense.getAmount() != null) {
                total = total.add(expense.getAmount());
            }
        }

        return total;
    }
}
