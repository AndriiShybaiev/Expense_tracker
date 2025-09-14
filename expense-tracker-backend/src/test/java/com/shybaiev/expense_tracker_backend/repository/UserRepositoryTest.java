package com.shybaiev.expense_tracker_backend.repository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import com.shybaiev.expense_tracker_backend.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndFindUserByUsername() {
        User user = User.builder()
                .username("john")
                .email("john@example.com")
                .passwordHash("hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd")
                .build();

        userRepository.save(user);

        Optional<User> found = userRepository.findByUsername("john");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void testSaveAndFindUserByEmail() {
        User user = User.builder()
                .username("john")
                .email("john@example.com")
                .passwordHash("hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd_hashed_pwd")
                .build();

        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("john@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("john");
    }
}
