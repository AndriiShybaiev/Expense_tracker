package com.shybaiev.expense_tracker_backend.mapper;

import com.shybaiev.expense_tracker_backend.dto.UserDto;
import com.shybaiev.expense_tracker_backend.dto.UserCreateUpdateDto;
import com.shybaiev.expense_tracker_backend.dto.UserRegisterDto;
import com.shybaiev.expense_tracker_backend.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setEnabled(user.isEnabled());
        return dto;
    }

    public User updateToEntity(UserCreateUpdateDto dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPasswordHash(dto.getPassword());
        user.setRole(dto.getRole());
        if (dto.getEnabled() != null) {
            user.setEnabled(dto.getEnabled());
        }
        return user;
    }

    public User registerToEntity(UserRegisterDto dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPasswordHash(dto.getPassword());
        return user;
    }
}
