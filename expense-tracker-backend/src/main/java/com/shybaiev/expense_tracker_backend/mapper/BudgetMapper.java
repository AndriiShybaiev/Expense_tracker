package com.shybaiev.expense_tracker_backend.mapper;

import com.shybaiev.expense_tracker_backend.dto.BudgetCreateUpdateDto;
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

    public Budget toEntity(BudgetCreateUpdateDto budgetCreateUpdateDto) {
        Budget budget = new Budget();
        budget.setAmount(budgetCreateUpdateDto.getAmount());
        budget.setName(budgetCreateUpdateDto.getName());
        budget.setDescription(budgetCreateUpdateDto.getDescription());
        budget.setTimePeriod(budgetCreateUpdateDto.getTimePeriod());
        budget.setStartDate(budgetCreateUpdateDto.getStartDate());
        User user = new User();
        user.setId(budgetCreateUpdateDto.getUserId());
        budget.setUser(user);
        return budget;
    }
}
