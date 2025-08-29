package com.shybaiev.expense_tracker_backend.repository;

import com.shybaiev.expense_tracker_backend.entity.Budget;
import com.shybaiev.expense_tracker_backend.entity.Expense;
import com.shybaiev.expense_tracker_backend.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
@DataJpaTest
class BudgetRepositoryTest {
    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Test
    void testFindAllByUser() {
        // given
        User user = new User();
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setPasswordHash("hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd");
        user = userRepository.save(user);

        Budget budget = new Budget();
        budget.setUser(user);
        budget.setTimePeriod("MONTHLY");
        budget.setStartDate(java.time.LocalDate.now());
        budget.setAmount(BigDecimal.valueOf(1000));
        budget.setName("My Monthly Budget");
        budget = budgetRepository.save(budget);

        Expense expense = new Expense();
        expense.setUser(user);
        expense.setBudget(budget);
        expense.setAmount(BigDecimal.valueOf(100));
        expense.setCategory("Food");
        expense.setSource("Cash");
        expense.setDescription("Groceries");
        expense.setPlace("Supermarket");
        expense.setTimestamp(OffsetDateTime.now()
        );
        expenseRepository.save(expense);

        // when
        List<Budget> budgets = budgetRepository.findAllByUser(user);

        // then
        assertThat(budgets).hasSize(1);
        assertThat(budgets.getFirst().getAmount().intValueExact()).isEqualTo(1000);
    }

    @Test
    void testFindByExpenses() {
        // given
        User user = new User();
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setPasswordHash("hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd");
        user = userRepository.save(user);

        Budget budget = new Budget();
        budget.setUser(user);
        budget.setTimePeriod("MONTHLY");
        budget.setStartDate(java.time.LocalDate.now());
        budget.setAmount(BigDecimal.valueOf(1000));
        budget.setName("My Monthly Budget");
        budget = budgetRepository.save(budget);

        Expense expense = new Expense();
        expense.setUser(user);
        expense.setBudget(budget);
        expense.setAmount(BigDecimal.valueOf(100));
        expense.setCategory("Food");
        expense.setSource("Cash");
        expense.setDescription("Groceries");
        expense.setPlace("Supermarket");
        expense.setTimestamp(OffsetDateTime.now()
        );
        expenseRepository.save(expense);

        // when
        Optional<Budget> foundBudget = budgetRepository.findByExpenses(expense);
        // then
        foundBudget.ifPresent(foundExistingBudget -> assertThat(foundExistingBudget.getAmount().intValueExact()).isEqualTo(1000));



    }
}