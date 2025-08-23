package com.shybaiev.expense_tracker_backend.entity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(
            mappedBy = "budget",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    @JsonManagedReference
    @Valid
    private List<Expense> expenses = new ArrayList<>();
}
