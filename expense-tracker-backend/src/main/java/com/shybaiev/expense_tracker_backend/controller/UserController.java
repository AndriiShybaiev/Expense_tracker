package com.shybaiev.expense_tracker_backend.controller;
import com.shybaiev.expense_tracker_backend.dto.ExpenseDto;
import com.shybaiev.expense_tracker_backend.dto.UserDto;
import com.shybaiev.expense_tracker_backend.dto.UserCreateUpdateDto;
import com.shybaiev.expense_tracker_backend.entity.Expense;
import com.shybaiev.expense_tracker_backend.entity.User;
import com.shybaiev.expense_tracker_backend.mapper.ExpenseMapper;
import com.shybaiev.expense_tracker_backend.mapper.UserMapper;
import com.shybaiev.expense_tracker_backend.service.ExpenseService;
import com.shybaiev.expense_tracker_backend.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final ExpenseService expenseService;
    private final ExpenseMapper expenseMapper;

    //admin only
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> createUser(@RequestBody UserCreateUpdateDto userCreateUpdateDto) {
        User user = userMapper.updateToEntity(userCreateUpdateDto);
        user.setEnabled(true);
        User saved = userService.createUser(user);
        URI location = URI.create("/users/" + saved.getId());
        return ResponseEntity.created(location).body(userMapper.toDto(saved));
    }

    //yourself or admin
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.getId()")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> maybeUser = userService.getUserById(id);
        if (maybeUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !maybeUser.get().getEmail().equals(userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(userMapper.toDto(maybeUser.get()));
    }

    //yourself or admin, but not admin himself
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.getId()")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin && userService.getUserById(id)
                .map(u -> u.getEmail().equals(userDetails.getUsername()))
                .orElse(false)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // admin cannot delete himself
        }

        // if user delete only if himself
        if (!isAdmin && !userService.getUserById(id)
                .map(u -> u.getEmail().equals(userDetails.getUsername()))
                .orElse(false)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.getId()"
    )
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id,
                                              @RequestBody UserCreateUpdateDto userCreateUpdateDto,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        User existingUser = userService.getUserById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !existingUser.getEmail().equals(userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        existingUser.setUsername(userCreateUpdateDto.getUsername());
        existingUser.setEmail(userCreateUpdateDto.getEmail());
        if (userCreateUpdateDto.getPassword() != null) {
            existingUser.setPasswordHash(userCreateUpdateDto.getPassword());
        }

        if (isAdmin && userCreateUpdateDto.getRole() != null) {
            existingUser.setRole(userCreateUpdateDto.getRole());
        }

        User updated = userService.updateUser(id, existingUser);
        return ResponseEntity.ok(userMapper.toDto(updated));
    }


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // ADMIN access only
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserDto> result = new ArrayList<>(users.size());
        for (User u : users) {
            result.add(userMapper.toDto(u));
        }
        return ResponseEntity.ok(result);
    }


    @GetMapping("/{id}/expenses")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.getId()")
    public ResponseEntity<List<ExpenseDto>> getAllExpensesByUser(@PathVariable Long id,
                                                                 @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !user.getEmail().equals(userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<Expense> expenses = expenseService.getAllExpensesForUser(user.getEmail());
        List<ExpenseDto> result = new ArrayList<>();
        for (Expense expense : expenses) {
            result.add(expenseMapper.toDto(expense));
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> maybeUser = userService.getUserByEmail(userDetails.getUsername());
        if (maybeUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userMapper.toDto(maybeUser.get()));
    }

}