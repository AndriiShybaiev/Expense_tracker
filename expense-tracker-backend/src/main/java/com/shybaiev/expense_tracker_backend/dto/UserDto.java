package com.shybaiev.expense_tracker_backend.dto;

import com.shybaiev.expense_tracker_backend.entity.Role;
import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private boolean enabled;
}
