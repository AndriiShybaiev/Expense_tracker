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
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;


@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;
    private final ExpenseMapper expenseMapper;

    @PostMapping
    public ResponseEntity<ExpenseDto> addExpense(@RequestBody ExpenseCreateUpdateDto expenseCreateUpdateDto) {
        Expense expense = expenseMapper.toEntity(expenseCreateUpdateDto);
        Expense saved = expenseService.createExpense(expense);
        URI location = URI.create("/expenses/" + saved.getId());
        return ResponseEntity.created(location).body(expenseMapper.toDto(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDto> getExpenseById(@PathVariable Long id) {
        Optional<Expense> maybeExpense = expenseService.getExpenseById(id);
        if (maybeExpense.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        ExpenseDto body = expenseMapper.toDto(maybeExpense.get());
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpenseById(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build(); // code 204 No Content
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ExpenseDto> updateExpense(@PathVariable Long id, @RequestBody ExpenseCreateUpdateDto expenseCreateUpdateDto) {
        Expense expense = expenseMapper.toEntity(expenseCreateUpdateDto);
        try {
            Expense updated = expenseService.updateExpense(id, expense);
            return ResponseEntity.ok(expenseMapper.toDto(updated));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

}