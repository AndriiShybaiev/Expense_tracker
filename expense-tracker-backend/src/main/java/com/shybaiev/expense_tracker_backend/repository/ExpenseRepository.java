package com.shybaiev.expense_tracker_backend.repository;

import com.shybaiev.expense_tracker_backend.entity.Expense;
import com.shybaiev.expense_tracker_backend.entity.User;
import com.shybaiev.expense_tracker_backend.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findAllByUser(User user);
    List<Expense> findAllByBudget(Budget budget);
    List<Expense> findAllByUserAndBudget(User user, Budget budget);
    List<Expense> findAllByCategoryAndUser(String category, User user);
    List<Expense> findAllByTimestampBetweenAndUser(OffsetDateTime from, OffsetDateTime to, User user);

    @Query("""
        SELECT COALESCE(SUM(e.amount), 0)
        FROM Expense e
        WHERE e.budget = :budget
        """)
    BigDecimal getTotalExpensesByBudget(Budget budget);

    @Query("""
       select coalesce(sum(e.amount), 0)
       from Expense e
       where e.budget = :budget and e.user = :user
       """)
    BigDecimal getTotalExpensesByBudgetAndUser(Budget budget, User user);
    List<Expense> findAllByUserId(Long userId);




}