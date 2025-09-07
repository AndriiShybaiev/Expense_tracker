package com.shybaiev.expense_tracker_backend.controller;

import com.shybaiev.expense_tracker_backend.dto.UserRegisterDto;
import com.shybaiev.expense_tracker_backend.entity.User;
import com.shybaiev.expense_tracker_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody UserRegisterDto dto) {
        User saved = userService.registerUser(dto);
        return ResponseEntity.created(URI.create("/users/" + saved.getId())).body(saved);
    }
}
