package com.shybaiev.expense_tracker_backend.service;

import com.shybaiev.expense_tracker_backend.entity.Budget;
import com.shybaiev.expense_tracker_backend.entity.Expense;
import com.shybaiev.expense_tracker_backend.entity.User;
import com.shybaiev.expense_tracker_backend.repository.ExpenseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository expenseRepository;

    public Expense createExpense(Expense expense){
        return expenseRepository.save(expense);
    }

    public Optional<Expense> getExpenseById(Long id){
        return expenseRepository.findById(id);
    }

    public Expense updateExpense(Long id, Expense updatedExpense){
        Optional<Expense> foundExpense = expenseRepository.findById(id);
        if (foundExpense.isPresent()){
            Expense existingExpense = foundExpense.get();
            if (updatedExpense.getAmount() != null) {
                existingExpense.setAmount(updatedExpense.getAmount());
            }
            if (updatedExpense.getDescription() != null) {
                existingExpense.setDescription(updatedExpense.getDescription());
            }
            if (updatedExpense.getPlace() != null) {
                existingExpense.setPlace(updatedExpense.getPlace());
            }
            if (updatedExpense.getCategory() != null) {
                existingExpense.setCategory(updatedExpense.getCategory());
            }
            if (updatedExpense.getSource() != null) {
                existingExpense.setSource(updatedExpense.getSource());
            }
            if (updatedExpense.getTimestamp() != null) {
                existingExpense.setTimestamp(updatedExpense.getTimestamp());
            }
            return expenseRepository.save(existingExpense);
        }
        else {
            throw new EntityNotFoundException("Expense with id " + id + " not found");
        }
    }

    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    public List<Expense> getAllExpensesByUser(User user){
        return expenseRepository.findAllByUser(user);
    }

    public List<Expense> getAllExpensesByBudget(Budget budget){
        return expenseRepository.findAllByBudget(budget);
    }

    public List<Expense> getExpensesByCategoryAndUser(String category, User user) {
        return expenseRepository.findAllByCategoryAndUser(category, user);
    }

    public List<Expense> getExpensesByDateRangeAndUser(OffsetDateTime from, OffsetDateTime to, User user) {
        return expenseRepository.findAllByTimestampBetweenAndUser(from, to, user);
    }

    public void deleteExpense(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new EntityNotFoundException("Expense with id " + id + " not found");
        }
        expenseRepository.deleteById(id);
    }


}
