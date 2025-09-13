package com.shybaiev.expense_tracker_backend.service;

import com.shybaiev.expense_tracker_backend.dto.BudgetCreateUpdateDto;
import com.shybaiev.expense_tracker_backend.entity.Budget;
import com.shybaiev.expense_tracker_backend.entity.Expense;
import com.shybaiev.expense_tracker_backend.entity.User;
import com.shybaiev.expense_tracker_backend.mapper.BudgetMapper;
import com.shybaiev.expense_tracker_backend.repository.BudgetRepository;
import com.shybaiev.expense_tracker_backend.repository.ExpenseRepository;
import com.shybaiev.expense_tracker_backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseService expenseService;

    private final UserRepository userRepository;
    private final BudgetMapper budgetMapper;


    public Budget createBudgetForUser(BudgetCreateUpdateDto dto, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Budget budget = budgetMapper.toEntity(dto);
        budget.setUser(user);

        return budgetRepository.save(budget);
    }

    public Optional<Budget> getBudgetByIdForUser(Long id, String userEmail) {
        Optional<User> maybeUser = userRepository.findByEmail(userEmail);
        if (maybeUser.isEmpty()) {
            throw new EntityNotFoundException("User not found: " + userEmail);
        }
        User user = maybeUser.get();

        Optional<Budget> maybeBudget = budgetRepository.findById(id);
        if (maybeBudget.isEmpty()) {
            return Optional.empty();
        }
        Budget budget = maybeBudget.get();
        if (budget.getUser() == null) {
            return Optional.empty();
        }
        if (Objects.equals(budget.getUser().getId(), user.getId())) {
            return Optional.of(budget);
        } else {
            // found, but doesnt belong to user
            return Optional.empty();
        }
    }

    public Budget updateBudgetForUser(Long id, BudgetCreateUpdateDto dto, String userEmail) {
        Optional<User> maybeUser = userRepository.findByEmail(userEmail);
        if (maybeUser.isEmpty()) {
            throw new EntityNotFoundException("User not found: " + userEmail);
        }
        User user = maybeUser.get();

        Optional<Budget> maybeBudget = budgetRepository.findById(id);
        if (maybeBudget.isEmpty()) {
            throw new EntityNotFoundException("Budget not found: " + id);
        }
        Budget existing = maybeBudget.get();

        if (existing.getUser() == null || !Objects.equals(existing.getUser().getId(), user.getId())) {
            throw new AccessDeniedException("You are not allowed to update this budget");
        }
        if (dto.getAmount() != null) {
            existing.setAmount(dto.getAmount());
        }
        if (dto.getName() != null) {
            existing.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            existing.setDescription(dto.getDescription());
        }
        if (dto.getTimePeriod() != null) {
            existing.setTimePeriod(dto.getTimePeriod());
        }
        if (dto.getStartDate() != null) {
            existing.setStartDate(dto.getStartDate());
        }

        return budgetRepository.save(existing);
    }

    public List<Budget> getAllBudgetsForUser(String userEmail) {
        Optional<User> maybeUser = userRepository.findByEmail(userEmail);
        if (maybeUser.isEmpty()) {
            throw new EntityNotFoundException("User not found: " + userEmail);
        }
        User user = maybeUser.get();
        return budgetRepository.findAllByUser(user);
    }

    public Budget getBudgetByExpenseForUser(Expense expense, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userEmail));

        Budget budget = budgetRepository.findByExpenses(expense)
                .orElseThrow(() -> new EntityNotFoundException("No budget found for expense id: " + expense.getId()));

        if (!budget.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not allowed to read this budget");
        }

        return budget;
    }

    public void deleteBudgetForUser(Long id, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userEmail));

        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Budget not found: " + id));

        if (!Objects.equals(budget.getUser().getId(), user.getId())) {
            throw new AccessDeniedException("You are not allowed to delete budget id=" + id);
        }

        budgetRepository.delete(budget);
    }


    public BigDecimal getTotalExpensesForBudgetForUser(Budget budget, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userEmail));

        if (!Objects.equals(budget.getUser().getId(), user.getId())) {
            throw new AccessDeniedException("You are not allowed to access this budget");
        }

        return expenseRepository.getTotalExpensesByBudget(budget);
    }


    @Transactional(readOnly = true)
    public boolean isUserOverBudgetInMonthForUser(String userEmail, YearMonth yearMonth) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userEmail));

        if (user.getBudgets().isEmpty()) {
            return false;
        }

        // в MVP максимум один бюджет
        Budget budget = user.getBudgets().getFirst();
        BigDecimal totalExpenses = expenseService.getTotalExpensesForUserInMonth(user, yearMonth);

        return totalExpenses.compareTo(budget.getAmount()) > 0;
    }



}
