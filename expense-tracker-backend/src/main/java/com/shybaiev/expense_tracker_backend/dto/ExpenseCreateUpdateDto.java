package com.shybaiev.expense_tracker_backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class ExpenseCreateUpdateDto {
    private BigDecimal amount;
    private String description;
    private String place;
    private String category;
    private String source;
    private OffsetDateTime timestamp;
    private Long budgetId;
    private Long userId;
}
