package com.shybaiev.expense_tracker_backend.controller;
import com.shybaiev.expense_tracker_backend.dto.UserDto;
import com.shybaiev.expense_tracker_backend.dto.UserUpdateDto;
import com.shybaiev.expense_tracker_backend.entity.User;
import com.shybaiev.expense_tracker_backend.mapper.UserMapper;
import com.shybaiev.expense_tracker_backend.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserUpdateDto userUpdateDto) {
        User user = userMapper.updateToEntity(userUpdateDto);
        user.setEnabled(true);
        User saved = userService.createUser(user);
        URI location = URI.create("/users/" + saved.getId());
        return ResponseEntity.created(location).body(userMapper.toDto(saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        Optional<User> maybeUser = userService.getUserById(id);
        if (maybeUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        UserDto body = userMapper.toDto(maybeUser.get());
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }


    @PatchMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody UserUpdateDto userUpdateDto) {
        User user = userMapper.updateToEntity(userUpdateDto);
        try {
            User updated = userService.updateUser(id, user);
            return ResponseEntity.ok(userMapper.toDto(updated));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
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