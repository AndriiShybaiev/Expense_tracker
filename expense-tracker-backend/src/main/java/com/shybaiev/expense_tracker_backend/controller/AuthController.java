package com.shybaiev.expense_tracker_backend.controller;

import com.shybaiev.expense_tracker_backend.dto.JwtResponse;
import com.shybaiev.expense_tracker_backend.dto.LoginRequestDto;
import com.shybaiev.expense_tracker_backend.dto.UserDto;
import com.shybaiev.expense_tracker_backend.dto.UserRegisterDto;
import com.shybaiev.expense_tracker_backend.entity.User;
import com.shybaiev.expense_tracker_backend.mapper.UserMapper;
import com.shybaiev.expense_tracker_backend.repository.UserRepository;
import com.shybaiev.expense_tracker_backend.security.JwtUtil;
import com.shybaiev.expense_tracker_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserMapper userMapper;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;


    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody UserRegisterDto dto) {
        User user = userService.registerUser(dto); // тут сервис делает всё сам
        UserDto responseDto = userMapper.toDto(user);
        URI location = URI.create("/users/" + user.getId());
        return ResponseEntity.created(location).body(responseDto);
    }



    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequestDto request) {
        User user = userService.authenticate(request.getEmail(), request.getPassword());
        String token = jwtUtil.generateToken(user);
        return ResponseEntity.ok(new JwtResponse(token));
    }

}
