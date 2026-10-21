package com.personalmanager.platform_service.service;

import com.personalmanager.platform_service.domain.model.EntitlementStatus;
import com.personalmanager.platform_service.domain.model.FeatureModule;
import com.personalmanager.platform_service.domain.model.UserEntitlement;
import com.personalmanager.platform_service.domain.repository.FeatureModuleRepository;
import com.personalmanager.platform_service.domain.repository.UserEntitlementRepository;
import com.personalmanager.platform_service.exception.EntitlementAlreadyExistsException;
import com.personalmanager.platform_service.exception.FeatureModuleNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserEntitlementServiceTest {

    @Mock
    private UserEntitlementRepository entitlementRepository;

    @Mock
    private FeatureModuleRepository featureRepository;

    private UserEntitlementService service;

    private FeatureModule feature;

    @BeforeEach
    void setUp() {
        service = new UserEntitlementService(
                entitlementRepository,
                featureRepository);

        feature = new FeatureModule(
                "PASSWORD_MANAGER",
                "Password Manager",
                "Secure credential storage");

        // Simulate a persisted feature with a generated ID.
        ReflectionTestUtils.setField(feature, "id", UUID.randomUUID());
    }

    @Test
    void grantShouldCreateEntitlementForEnabledFeature() {
        when(featureRepository.findByCode("PASSWORD_MANAGER"))
                .thenReturn(Optional.of(feature));

        when(entitlementRepository
                .existsByUserSubjectAndFeatureModule_Id(
                        "test-user-001", feature.getId()))
                .thenReturn(false);

        when(entitlementRepository.save(any(UserEntitlement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserEntitlement result = service.grant(
                " test-user-001 ",
                " password_manager ",
                Instant.parse("2026-01-01T00:00:00Z"),
                null);

        assertEquals("test-user-001", result.getUserSubject());
        assertEquals("PASSWORD_MANAGER",
                result.getFeatureModule().getCode());
        assertEquals(EntitlementStatus.ACTIVE, result.getStatus());
        assertNull(result.getValidUntil());

        verify(entitlementRepository).save(any(UserEntitlement.class));
    }

    @Test
    void grantShouldRejectDuplicateEntitlement() {
        when(featureRepository.findByCode("PASSWORD_MANAGER"))
                .thenReturn(Optional.of(feature));

        when(entitlementRepository
                .existsByUserSubjectAndFeatureModule_Id(
                        "test-user-001", feature.getId()))
                .thenReturn(true);

        assertThrows(
                EntitlementAlreadyExistsException.class,
                () -> service.grant(
                        "test-user-001",
                        "PASSWORD_MANAGER",
                        Instant.now(),
                        null));

        verify(entitlementRepository, never())
                .save(any(UserEntitlement.class));
    }

    @Test
    void grantShouldRejectUnknownFeature() {
        when(featureRepository.findByCode("UNKNOWN_FEATURE"))
                .thenReturn(Optional.empty());

        assertThrows(
                FeatureModuleNotFoundException.class,
                () -> service.grant(
                        "test-user-001",
                        "UNKNOWN_FEATURE",
                        Instant.now(),
                        null));

        verify(entitlementRepository, never())
                .save(any(UserEntitlement.class));
    }

    @Test
    void grantShouldRejectDisabledFeature() {
        feature.disable();

        when(featureRepository.findByCode("PASSWORD_MANAGER"))
                .thenReturn(Optional.of(feature));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.grant(
                        "test-user-001",
                        "PASSWORD_MANAGER",
                        Instant.now(),
                        null));

        verify(entitlementRepository, never())
                .save(any(UserEntitlement.class));
    }

    @Test
    void grantShouldRejectInvalidValidityRange() {
        Instant validFrom = Instant.parse("2026-01-02T00:00:00Z");
        Instant validUntil = Instant.parse("2026-01-01T00:00:00Z");

        assertThrows(
                IllegalArgumentException.class,
                () -> service.grant(
                        "test-user-001",
                        "PASSWORD_MANAGER",
                        validFrom,
                        validUntil));

        verifyNoInteractions(featureRepository);
        verifyNoInteractions(entitlementRepository);
    }

    @Test
    void grantShouldRejectBlankUserSubject() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.grant(
                        " ",
                        "PASSWORD_MANAGER",
                        Instant.now(),
                        null));

        verifyNoInteractions(featureRepository);
        verifyNoInteractions(entitlementRepository);
    }

    @Test
    void hasAccessShouldReturnTrueForEffectiveEntitlement() {
        UserEntitlement entitlement = new UserEntitlement(
                "test-user-001",
                feature,
                Instant.now().minusSeconds(60),
                null);

        when(entitlementRepository
                .findByUserSubjectAndFeatureModule_Code(
                        "test-user-001", "PASSWORD_MANAGER"))
                .thenReturn(Optional.of(entitlement));

        assertTrue(service.hasAccess(
                "test-user-001", "password_manager"));
    }

    @Test
    void hasAccessShouldReturnFalseForSuspendedEntitlement() {
        UserEntitlement entitlement = new UserEntitlement(
                "test-user-001",
                feature,
                Instant.now().minusSeconds(60),
                null);

        entitlement.suspend();

        when(entitlementRepository
                .findByUserSubjectAndFeatureModule_Code(
                        "test-user-001", "PASSWORD_MANAGER"))
                .thenReturn(Optional.of(entitlement));

        assertFalse(service.hasAccess(
                "test-user-001", "PASSWORD_MANAGER"));
    }

    @Test
    void hasAccessShouldReturnFalseWhenEntitlementDoesNotExist() {
        when(entitlementRepository
                .findByUserSubjectAndFeatureModule_Code(
                        "test-user-001", "PASSWORD_MANAGER"))
                .thenReturn(Optional.empty());

        assertFalse(service.hasAccess(
                "test-user-001", "PASSWORD_MANAGER"));
    }
}