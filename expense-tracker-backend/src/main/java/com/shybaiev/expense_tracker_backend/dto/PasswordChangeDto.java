package com.shybaiev.expense_tracker_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordChangeDto {

    @NotBlank
    private String oldPassword;

    @NotBlank
    @Size(min = 8, max = 255)
    private String newPassword;
}
