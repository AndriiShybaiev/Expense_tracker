package com.shybaiev.expense_tracker_backend.service;

import com.shybaiev.expense_tracker_backend.dto.UserRegisterDto;
import com.shybaiev.expense_tracker_backend.entity.Role;
import com.shybaiev.expense_tracker_backend.entity.User;
import com.shybaiev.expense_tracker_backend.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User updateUser(Long id, User updatedUser) {
        Optional<User> foundUser = userRepository.findById(id);
        if (foundUser.isPresent()) {
            User existingUser = foundUser.get();
            if (updatedUser.getUsername() != null) {
                existingUser.setUsername(updatedUser.getUsername());
            }
            if (updatedUser.getEmail() != null) {
                existingUser.setEmail(updatedUser.getEmail());
            }
            if (updatedUser.getPasswordHash() != null) {
                existingUser.setPasswordHash(updatedUser.getPasswordHash());
            }
            if (updatedUser.getRole() != null) {
                existingUser.setRole(updatedUser.getRole());
            }
            existingUser.setEnabled(updatedUser.isEnabled());
            return userRepository.save(existingUser);
        }
        else {
            throw new EntityNotFoundException("User with id " + id + " not found");
        }
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User with id " + id + " not found");
        }
        userRepository.deleteById(id);
    }


    public User registerUser(UserRegisterDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new EntityExistsException("Email already in use");
        }
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new EntityExistsException("Username already in use");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.USER);
        user.setEnabled(true);

        return userRepository.save(user);
    }

}
