package com.project.demo.logic.entity.user;

import com.project.demo.logic.entity.rol.Role;
import com.project.demo.logic.entity.rol.RoleEnum;
import com.project.demo.logic.entity.rol.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Role userRole;

    @BeforeEach
    void setUp() {
        // Ensure a Role exists for users, as it's a non-nullable association
        Role role = new Role();
        role.setName(RoleEnum.USER);
        role.setDescription("Default User Role"); // Set non-null description
        // We must persist the Role first if it's a new one for each test,
        // or ensure it's pre-loaded (e.g. via schema.sql or @Sql)
        // For DataJpaTest, it's often cleaner to persist dependencies.
        userRole = roleRepository.findByName(RoleEnum.USER)
                .orElseGet(() -> entityManager.persistFlushFind(role));
    }

    @Test
    void saveUser_success() {
        User user = new User();
        user.setName("Test");
        user.setLastname("User");
        user.setEmail("test.user@example.com");
        user.setPassword("password123");
        user.setRole(userRole);

        User savedUser = userRepository.save(user);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test.user@example.com");
        assertThat(savedUser.getCreatedAt()).isNotNull(); // Check if @CreationTimestamp works
        assertThat(savedUser.getUpdatedAt()).isNotNull(); // Check if @UpdateTimestamp works
    }

    @Test
    void findByEmail_userExists() {
        User user = new User();
        user.setName("Find Me");
        user.setLastname("By Email");
        user.setEmail("findme@example.com");
        user.setPassword("password");
        user.setRole(userRole);
        entityManager.persistAndFlush(user);

        Optional<User> foundUser = userRepository.findByEmail("findme@example.com");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Find Me");
    }

    @Test
    void findByEmail_userDoesNotExist() {
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");
        assertThat(foundUser).isNotPresent();
    }

    @Test
    void whenUserSaved_thenTimestampsAreSet() {
        User user = new User();
        user.setName("Timestamp");
        user.setLastname("Test");
        user.setEmail("timestamp@example.com");
        user.setPassword("password");
        user.setRole(userRole);

        User savedUser = userRepository.save(user);
        entityManager.flush(); // Ensure changes are written to DB
        entityManager.clear(); // Clear persistence context to force reload

        User reloadedUser = userRepository.findById(savedUser.getId()).orElseThrow();

        assertThat(reloadedUser.getCreatedAt()).isNotNull();
        assertThat(reloadedUser.getUpdatedAt()).isNotNull();

        // Further test for update
        try {
            // Small delay to ensure updatedAt might change if precision allows
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        reloadedUser.setLastname("Updated");
        userRepository.saveAndFlush(reloadedUser);
        entityManager.clear();

        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updatedUser.getUpdatedAt()).isAfterOrEqualTo(reloadedUser.getCreatedAt());
        // A more precise check would be difficult without knowing DB time precision
        // but we can check it's not null and potentially different if enough time passed.
        if (reloadedUser.getUpdatedAt() != null && updatedUser.getUpdatedAt() != null) {
             assertThat(updatedUser.getUpdatedAt()).isNotEqualTo(reloadedUser.getUpdatedAt());
        } else {
            // if first update was null (e.g. if created at and updated at were same initially)
            assertThat(updatedUser.getUpdatedAt()).isNotNull();
        }
    }
}
