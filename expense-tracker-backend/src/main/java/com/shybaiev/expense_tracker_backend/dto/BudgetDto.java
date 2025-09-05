package com.shybaiev.expense_tracker_backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BudgetDto {
    private Long id;
    private BigDecimal amount;
    private String name;
    private String description;
    private String timePeriod;
    private LocalDate startDate;
    private Long userId;
}
