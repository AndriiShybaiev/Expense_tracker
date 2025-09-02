package com.shybaiev.expense_tracker_backend.dto;

import com.shybaiev.expense_tracker_backend.entity.Role;
import lombok.Data;

@Data
public class UserUpdateDto {
    private String username;
    private String email;
    private String password;
    private Role role;
    private Boolean enabled;
}

