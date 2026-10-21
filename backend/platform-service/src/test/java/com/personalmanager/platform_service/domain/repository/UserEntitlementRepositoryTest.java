package com.personalmanager.platform_service.domain.repository;

import com.personalmanager.platform_service.domain.model.FeatureModule;
import com.personalmanager.platform_service.domain.model.UserEntitlement;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class UserEntitlementRepositoryTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("pm_platform_test")
                    .withUsername("pm_test")
                    .withPassword("pm_test");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserEntitlementRepository entitlementRepository;

    @Autowired
    private FeatureModuleRepository featureRepository;

    @Test
    void shouldPersistAndRetrieveEntitlement() {
        String suffix = UUID.randomUUID().toString();

        FeatureModule feature = featureRepository.saveAndFlush(
                new FeatureModule(
                        "TEST_FEATURE_" + suffix.replace("-", ""),
                        "Test Feature",
                        "Integration test feature"));

        UserEntitlement entitlement = entitlementRepository.saveAndFlush(
                new UserEntitlement(
                        "test-user-" + suffix,
                        feature,
                        Instant.now().minusSeconds(60),
                        null));

        assertNotNull(entitlement.getId());
        assertNotNull(entitlement.getCreatedAt());
        assertNotNull(entitlement.getUpdatedAt());

        UserEntitlement retrieved = entitlementRepository
                .findByUserSubjectAndFeatureModule_Code(
                        "test-user-" + suffix,
                        feature.getCode())
                .orElseThrow();

        assertEquals(entitlement.getId(), retrieved.getId());
        assertEquals("test-user-" + suffix, retrieved.getUserSubject());
        assertEquals(feature.getId(),
                retrieved.getFeatureModule().getId());
        assertEquals("ACTIVE", retrieved.getStatus().name());
    }

    @Test
    void shouldRejectDuplicateUserAndFeatureEntitlement() {
        String suffix = UUID.randomUUID().toString();
        String subject = "duplicate-test-" + suffix;

        FeatureModule feature = featureRepository.saveAndFlush(
                new FeatureModule(
                        "DUPLICATE_TEST_" + suffix.replace("-", ""),
                        "Duplicate Test Feature",
                        null));

        entitlementRepository.saveAndFlush(
                new UserEntitlement(
                        subject,
                        feature,
                        Instant.now().minusSeconds(60),
                        null));

        UserEntitlement duplicate = new UserEntitlement(
                subject,
                feature,
                Instant.now().minusSeconds(60),
                null);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> entitlementRepository.saveAndFlush(duplicate));
    }
}