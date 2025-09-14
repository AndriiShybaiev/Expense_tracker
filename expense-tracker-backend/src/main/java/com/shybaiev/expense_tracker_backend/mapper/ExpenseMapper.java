package com.shybaiev.expense_tracker_backend.mapper;

import com.shybaiev.expense_tracker_backend.dto.ExpenseCreateUpdateDto;
import com.shybaiev.expense_tracker_backend.dto.ExpenseDto;
import com.shybaiev.expense_tracker_backend.entity.Budget;
import com.shybaiev.expense_tracker_backend.entity.Expense;
import com.shybaiev.expense_tracker_backend.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ExpenseMapper {

    public ExpenseDto toDto(Expense expense) {
        ExpenseDto expenseDto = new ExpenseDto();
        expenseDto.setId(expense.getId());
        expenseDto.setAmount(expense.getAmount());
        expenseDto.setDescription(expense.getDescription());
        expenseDto.setPlace(expense.getPlace());
        expenseDto.setCategory(expense.getCategory());
        expenseDto.setSource(expense.getSource());
        expenseDto.setTimestamp(expense.getTimestamp());
        expenseDto.setBudgetId(expense.getBudget() != null ? expense.getBudget().getId() : null);
        expenseDto.setUserId(expense.getUser() != null ? expense.getUser().getId() : null);
        return expenseDto;
    }
    public Expense toEntity(ExpenseCreateUpdateDto expenseDto) {
        Expense expense = new Expense();
        expense.setAmount(expenseDto.getAmount());
        expense.setDescription(expenseDto.getDescription());
        expense.setPlace(expenseDto.getPlace());
        expense.setCategory(expenseDto.getCategory());
        expense.setSource(expenseDto.getSource());
        if (expenseDto.getBudgetId() != null) {
            Budget budget = new Budget();
            budget.setId(expenseDto.getBudgetId());
            expense.setBudget(budget);
        }

        if (expenseDto.getUserId() != null) {
            User user = new User();
            user.setId(expenseDto.getUserId());
            expense.setUser(user);
        }

        return expense;

    }
}
