package com.project.demo.logic.entity.rol;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RoleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void findByName_roleExists() {
        Role userRole = new Role();
        userRole.setName(RoleEnum.USER);
        userRole.setDescription("Default user role");
        entityManager.persistAndFlush(userRole);

        Optional<Role> foundRole = roleRepository.findByName(RoleEnum.USER);

        assertThat(foundRole).isPresent();
        assertThat(foundRole.get().getName()).isEqualTo(RoleEnum.USER);
        assertThat(foundRole.get().getDescription()).isEqualTo("Default user role");
    }

    @Test
    void findByName_roleDoesNotExist() {
        // Ensure no ADMIN role is persisted by default for this test
        Optional<Role> foundRole = roleRepository.findByName(RoleEnum.ADMIN);
        assertThat(foundRole).isNotPresent();
    }

    @Test
    void saveRole_success() {
        Role adminRole = new Role();
        adminRole.setName(RoleEnum.ADMIN);
        adminRole.setDescription("Administrator role");

        Role savedRole = roleRepository.save(adminRole);
        entityManager.flush(); // Ensure all pending operations are executed
        entityManager.clear(); // Detach all entities so that subsequent find re-fetches from DB

        Role retrievedRole = roleRepository.findById(savedRole.getId()).orElse(null);

        assertThat(retrievedRole).isNotNull();
        assertThat(retrievedRole.getId()).isNotNull();
        assertThat(retrievedRole.getName()).isEqualTo(RoleEnum.ADMIN);
        assertThat(retrievedRole.getCreatedAt()).isNotNull();
        assertThat(retrievedRole.getUpdatedAt()).isNotNull();
        assertThat(retrievedRole.getDescription()).isEqualTo("Administrator role");
    }
}
