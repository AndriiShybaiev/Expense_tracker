package com.shybaiev.expense_tracker_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRegisterDto {
    @NotBlank
    private String username;
    @NotBlank
    private String email;
    @NotBlank
    private String password;
}
