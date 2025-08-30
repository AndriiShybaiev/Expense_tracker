package com.shybaiev.expense_tracker_backend.service;

import com.shybaiev.expense_tracker_backend.entity.Role;
import com.shybaiev.expense_tracker_backend.entity.User;
import com.shybaiev.expense_tracker_backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("oldName");
        existingUser.setEmail("old@email.com");
        existingUser.setPasswordHash("oldPass");
        existingUser.setRole(Role.USER);
        existingUser.setEnabled(true);
    }

    @Test
    void testCreateUser() {
        //given
        //setUp
        //when
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        //then
        assertEquals(existingUser, userService.createUser(existingUser));
        verify(userRepository).save(existingUser);

    }

    @Test
    void testGetUserById() {
        // given
        // setUp
        // when
        when(userRepository.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));
        Optional<User> result = userService.getUserById(existingUser.getId());
        //then
        assertTrue(result.isPresent());
        assertEquals(existingUser, result.get());

    }

    @Test
    void testUpdateUserSuccess() {
        // given
        User updatedUser = new User();
        updatedUser.setUsername("newName");
        updatedUser.setEmail("new@email.com");
        updatedUser.setPasswordHash("newPass");
        updatedUser.setRole(Role.ADMIN);
        updatedUser.setEnabled(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        User result = userService.updateUser(1L, updatedUser);

        // then
        assertEquals("newName", result.getUsername());
        assertEquals("new@email.com", result.getEmail());
        assertEquals("newPass", result.getPasswordHash());
        assertEquals(Role.ADMIN, result.getRole());
        assertFalse(result.isEnabled());

        verify(userRepository).findById(1L);
        verify(userRepository).save(existingUser);
    }

    @Test
    void testUpdateUserNotFound() {
        // given
        User updatedUser = new User();
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // when + then
        assertThrows(EntityNotFoundException.class,
                () -> userService.updateUser(999L, updatedUser));

        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testDeleteUserSuccess() {
        // given
        when(userRepository.existsById(existingUser.getId())).thenReturn(true);

        // when
        userService.deleteUser(existingUser.getId());

        // then
        verify(userRepository).existsById(existingUser.getId());
        verify(userRepository).deleteById(existingUser.getId());
    }

    @Test
    void testDeleteUserNotFound() {
        // given
        Long nonExistentUserId = 999L;
        when(userRepository.existsById(nonExistentUserId)).thenReturn(false);

        // when + then
        assertThrows(EntityNotFoundException.class,
                () -> userService.deleteUser(nonExistentUserId));

        verify(userRepository).existsById(nonExistentUserId);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void testGetAllUsers() {
        // given
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setEmail("user1@gmail.com");
        user1.setPasswordHash("pass1");
        user1.setRole(Role.USER);
        user1.setEnabled(true);

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@gmail.com");
        user2.setPasswordHash("pass2");
        user2.setRole(Role.ADMIN);
        user2.setEnabled(false);

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        // when
        List<User> result = userService.getAllUsers();

        // then
        assertEquals(2, result.size());
        assertTrue(result.contains(user1));
        assertTrue(result.contains(user2));
        verify(userRepository).findAll();
    }

    @Test
    void testGetAllUsersEmpty() {
        // given
        when(userRepository.findAll()).thenReturn(List.of());

        // when
        List<User> result = userService.getAllUsers();

        // then
        assertTrue(result.isEmpty());
        verify(userRepository).findAll();
    }
}
