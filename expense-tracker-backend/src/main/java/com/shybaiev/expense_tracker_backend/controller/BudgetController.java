package com.shybaiev.expense_tracker_backend.controller;

import com.shybaiev.expense_tracker_backend.dto.BudgetCreateUpdateDto;
import com.shybaiev.expense_tracker_backend.dto.BudgetDto;
import com.shybaiev.expense_tracker_backend.entity.Budget;
import com.shybaiev.expense_tracker_backend.mapper.BudgetMapper;
import com.shybaiev.expense_tracker_backend.service.BudgetService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;
    private final BudgetMapper budgetMapper;

    @PostMapping
    public ResponseEntity<BudgetDto> addBudget(@RequestBody BudgetCreateUpdateDto budgetCreateUpdateDto) {
        Budget budget = budgetMapper.toEntity(budgetCreateUpdateDto);
        Budget saved = budgetService.createBudget(budget);
        URI location = URI.create("/budgets/" + saved.getId());
        return ResponseEntity.created(location).body(budgetMapper.toDto(saved));
    }
    @GetMapping("/{id}")
    public ResponseEntity<BudgetDto> getBudgetById(@PathVariable Long id) {
        Optional<Budget> maybeBudget = budgetService.getBudgetById(id);
        if (maybeBudget.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        BudgetDto body = budgetMapper.toDto(maybeBudget.get());
        return ResponseEntity.ok(body);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudgetById(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
    @PatchMapping("/{id}")
    public ResponseEntity<BudgetDto> updateBudget(@PathVariable Long id,
                                                  @Valid @RequestBody BudgetCreateUpdateDto budgetCreateUpdateDto) {
        Budget budget = budgetMapper.toEntity(budgetCreateUpdateDto);
        try {
            Budget updated = budgetService.updateBudget(id, budget);
            return ResponseEntity.ok(budgetMapper.toDto(updated));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping
    public ResponseEntity<List<BudgetDto>> getAllBudgets() {
        List<Budget> budgets = budgetService.getAllBudgets();
        List<BudgetDto> result = new ArrayList<>(budgets.size());
        for (Budget b : budgets) {
            result.add(budgetMapper.toDto(b));
        }
        return ResponseEntity.ok(result);
    }
    @GetMapping("/budgets/{id}/expenses/total")
    public ResponseEntity<BigDecimal> getTotalExpensesForBudget(@PathVariable Long id) {
        Optional<Budget> maybeBudget = budgetService.getBudgetById(id);
        if (maybeBudget.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        BigDecimal total = budgetService.getTotalExpensesForBudget(maybeBudget.get());
        return ResponseEntity.ok(total);
    }

}

