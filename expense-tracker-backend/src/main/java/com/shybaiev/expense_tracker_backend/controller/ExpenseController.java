// ExpenseController.java
package com.shybaiev.expense_tracker_backend.controller;

import com.shybaiev.expense_tracker_backend.dto.ExpenseCreateUpdateDto;
import com.shybaiev.expense_tracker_backend.dto.ExpenseDto;
import com.shybaiev.expense_tracker_backend.entity.Expense;
import com.shybaiev.expense_tracker_backend.mapper.ExpenseMapper;
import com.shybaiev.expense_tracker_backend.service.ExpenseService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;
    private final ExpenseMapper expenseMapper;

    @PostMapping
    public ResponseEntity<ExpenseDto> addExpense(@RequestBody ExpenseCreateUpdateDto expenseCreateUpdateDto,
                                                 @AuthenticationPrincipal UserDetails user) {
        String email = user.getUsername();
        Expense saved = expenseService.createExpenseForUser(expenseCreateUpdateDto, email);
        URI location = URI.create("/expenses/" + saved.getId());
        return ResponseEntity.created(location).body(expenseMapper.toDto(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDto> getExpenseById(@PathVariable Long id,
                                                     @AuthenticationPrincipal UserDetails user) {
        String email = user.getUsername();
        Optional<Expense> maybeExpense = expenseService.getExpenseByIdForUser(id, email);
        if (maybeExpense.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        ExpenseDto body = expenseMapper.toDto(maybeExpense.get());
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpenseById(@PathVariable Long id,
                                                  @AuthenticationPrincipal UserDetails user) {
        String email = user.getUsername();
        try {
            expenseService.deleteExpenseForUser(id, email);
            return ResponseEntity.noContent().build(); // code 204 No Content
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).build(); // 403 Forbidden
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ExpenseDto> updateExpense(@PathVariable Long id, 
                                                    @RequestBody ExpenseCreateUpdateDto expenseCreateUpdateDto,
                                                    @AuthenticationPrincipal UserDetails user) {
        String email = user.getUsername();
        try {
            Expense updated = expenseService.updateExpenseForUser(id, expenseCreateUpdateDto, email);
            return ResponseEntity.ok(expenseMapper.toDto(updated));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).build(); // 403 Forbidden
        }
    }

    @GetMapping
    public ResponseEntity<List<ExpenseDto>> getAllExpenses(@AuthenticationPrincipal UserDetails user) {
        String email = user.getUsername();
        List<Expense> expenses = expenseService.getAllExpensesForUser(email);
        List<ExpenseDto> result = new ArrayList<>(expenses.size());
        for (Expense expense : expenses) {
            result.add(expenseMapper.toDto(expense));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ExpenseDto>> getExpensesByCategory(@PathVariable String category,
                                                                  @AuthenticationPrincipal UserDetails user) {
        String email = user.getUsername();
        List<Expense> expenses = expenseService.getExpensesByCategoryForUser(category, email);
        List<ExpenseDto> result = new ArrayList<>(expenses.size());
        for (Expense expense : expenses) {
            result.add(expenseMapper.toDto(expense));
        }
        return ResponseEntity.ok(result);
    }
}