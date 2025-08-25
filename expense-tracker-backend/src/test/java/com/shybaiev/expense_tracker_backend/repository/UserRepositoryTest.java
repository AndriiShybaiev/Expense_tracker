package com.shybaiev.expense_tracker_backend.repository;

import com.shybaiev.expense_tracker_backend.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndFindUser() {
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
}
