package com.shybaiev.expense_tracker_backend.service;

import com.shybaiev.expense_tracker_backend.entity.Budget;
import com.shybaiev.expense_tracker_backend.entity.Expense;
import com.shybaiev.expense_tracker_backend.entity.User;
import com.shybaiev.expense_tracker_backend.repository.BudgetRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;

    public Budget createBudget(Budget budget) {
        return budgetRepository.save(budget);
    }

    public Optional<Budget> getBudgetById(Long id) {
        return budgetRepository.findById(id);
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

    public List<Budget> getAllBudgets() {
        return budgetRepository.findAll();
    }

    public List<Budget> getAllBudgetsByUser(User user) {
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
}
