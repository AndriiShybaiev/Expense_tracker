package com.shybaiev.expense_tracker_backend.mapper;

import com.shybaiev.expense_tracker_backend.dto.BudgetDto;
import com.shybaiev.expense_tracker_backend.entity.Budget;
import com.shybaiev.expense_tracker_backend.entity.User;
import org.springframework.stereotype.Component;

@Component
public class BudgetMapper {
    public BudgetDto toDto(Budget budget) {
        BudgetDto budgetDto = new BudgetDto();
        budgetDto.setId(budget.getId());
        budgetDto.setAmount(budget.getAmount());
        budgetDto.setName(budget.getName());
        budgetDto.setDescription(budget.getDescription());
        budgetDto.setTimePeriod(budget.getTimePeriod());
        budgetDto.setStartDate(budget.getStartDate());
        budgetDto.setUserId(budget.getUser().getId());
        return budgetDto;
    }

    public Budget toEntity(BudgetDto budgetDto) {
        Budget budget = new Budget();
        budget.setId(budgetDto.getId());
        budget.setAmount(budgetDto.getAmount());
        budget.setName(budgetDto.getName());
        budget.setDescription(budgetDto.getDescription());
        budget.setTimePeriod(budgetDto.getTimePeriod());
        budget.setStartDate(budgetDto.getStartDate());
        User user = new User();
        user.setId(budgetDto.getUserId());
        budget.setUser(user);
        return budget;
    }
}
