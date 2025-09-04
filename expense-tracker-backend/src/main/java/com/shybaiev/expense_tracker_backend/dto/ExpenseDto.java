package com.shybaiev.expense_tracker_backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class ExpenseDto {
    private Long id;
    private BigDecimal amount;
    private String description;
    private String place;
    private String category;
    private String source;
    private Instant dateTime;
    private Long budgetId;
    private Long userId;
}
