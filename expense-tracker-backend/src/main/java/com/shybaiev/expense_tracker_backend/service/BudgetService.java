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

    public Budget createBudget(Budget budget) {
        return budgetRepository.save(budget);
    }

    public Budget createBudgetForUser(BudgetCreateUpdateDto dto, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Budget budget = budgetMapper.toEntity(dto);
        budget.setUser(user);

        return budgetRepository.save(budget);
    }

    public Optional<Budget> getBudgetById(Long id) {
        return budgetRepository.findById(id);
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

    public Budget updateBudget(Long existingBudgetId, Budget updatedBudget) {
        Optional<Budget> foundBudget = budgetRepository.findById(existingBudgetId);
        if (foundBudget.isPresent()) {
            Budget existingBudget = foundBudget.get();
            if (updatedBudget.getAmount() != null) {
                existingBudget.setAmount(updatedBudget.getAmount());
            }
            if (updatedBudget.getName() != null) {
                existingBudget.setName(updatedBudget.getName());
            }
            if (updatedBudget.getDescription() != null) {
                existingBudget.setDescription(updatedBudget.getDescription());
            }
            if (updatedBudget.getTimePeriod() != null) {
                existingBudget.setTimePeriod(updatedBudget.getTimePeriod());
            }
            if (updatedBudget.getStartDate() != null) {
                existingBudget.setStartDate(updatedBudget.getStartDate());
            }
            return budgetRepository.save(existingBudget);
        }
        else {
            throw new EntityNotFoundException("Budget with id " + existingBudgetId + " not found");
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



    public List<Budget> getAllBudgets() {
        return budgetRepository.findAll();
    }

    public List<Budget> getAllBudgetsByUser(User user) {
        return budgetRepository.findAllByUser(user);
    }

    public List<Budget> getAllBudgetsForUser(String userEmail) {
        Optional<User> maybeUser = userRepository.findByEmail(userEmail);
        if (maybeUser.isEmpty()) {
            throw new EntityNotFoundException("User not found: " + userEmail);
        }
        User user = maybeUser.get();
        return budgetRepository.findAllByUser(user);
    }

    public Budget getBudgetByExpense(Expense expense) {
        if (budgetRepository.findByExpenses(expense).isPresent()) {
            return budgetRepository.findByExpenses(expense).get();
        }
        else {
            throw new EntityNotFoundException("Expense with id " + expense.getId() + " not found");
        }
    }
    public void deleteBudget(Long id) {
        if (!budgetRepository.existsById(id)) {
            throw new EntityNotFoundException("Budget with id " + id + " not found");
        }
        budgetRepository.deleteById(id);
    }

    public void deleteBudgetForUser(Long id, String userEmail) {
        Optional<User> maybeUser = userRepository.findByEmail(userEmail);
        if (maybeUser.isEmpty()) {
            throw new EntityNotFoundException("User not found: " + userEmail);
        }
        User user = maybeUser.get();
        Optional<Budget> maybeBudget = budgetRepository.findById(id);
        if (maybeBudget.isEmpty()) {
            throw new EntityNotFoundException("Budget not found: " + id);
        }
        Budget budget = maybeBudget.get();

        if (budget.getUser() == null || !Objects.equals(budget.getUser().getId(), user.getId())) {
            throw new AccessDeniedException("You are not allowed to delete this budget");
        }

        budgetRepository.delete(budget);
    }

    public BigDecimal getTotalExpensesForBudget(Budget budget) {
        return expenseRepository.getTotalExpensesByBudget(budget);
    }

    @Transactional(readOnly = true)
    public boolean isUserOverBudgetInMonth(User user, YearMonth yearMonth) {
        if (user.getBudgets().isEmpty()) {
            return false;
        }
        Budget budget = user.getBudgets().getFirst(); // в MVP максимум один
        BigDecimal totalExpenses = expenseService.getTotalExpensesForUserInMonth(user, yearMonth);

        return totalExpenses.compareTo(budget.getAmount()) > 0;
    }


}
