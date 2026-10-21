package com.personalmanager.platform_service.domain.repository;

import com.personalmanager.platform_service.domain.model.FeatureModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class FeatureModuleRepositoryTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("pm_platform_test")
                    .withUsername("pm_test")
                    .withPassword("pm_test");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.datasource.url",
                postgres::getJdbcUrl);
        registry.add(
                "spring.datasource.username",
                postgres::getUsername);
        registry.add(
                "spring.datasource.password",
                postgres::getPassword);
    }

    @Autowired
    private FeatureModuleRepository repository;

    @Test
    void shouldPersistAndRetrieveFeatureModule() {
        FeatureModule module = new FeatureModule(
                "PASSWORD_MANAGER",
                "Password Manager",
                "Securely store encrypted credentials");

        FeatureModule saved = repository.saveAndFlush(module);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());

        FeatureModule retrieved = repository
                .findByCode("PASSWORD_MANAGER")
                .orElseThrow();

        assertEquals(saved.getId(), retrieved.getId());
        assertEquals("Password Manager", retrieved.getDisplayName());
        assertTrue(retrieved.isEnabled());
    }
}