package com.shybaiev.expense_tracker_backend.controller;
import com.shybaiev.expense_tracker_backend.dto.UserDto;
import com.shybaiev.expense_tracker_backend.entity.User;
import com.shybaiev.expense_tracker_backend.mapper.UserMapper;
import com.shybaiev.expense_tracker_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        Optional<User> maybeUser = userService.getUserById(id);
        if (maybeUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        UserDto body = userMapper.toDto(maybeUser.get());
        return ResponseEntity.ok(body);
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserDto> result = new ArrayList<>(users.size());
        for (User u : users) {
            result.add(userMapper.toDto(u));
        }
        return ResponseEntity.ok(result);
    }
}