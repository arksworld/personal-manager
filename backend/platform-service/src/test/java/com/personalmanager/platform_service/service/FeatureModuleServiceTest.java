package com.personalmanager.platform_service.service;

import com.personalmanager.platform_service.domain.model.FeatureModule;
import com.personalmanager.platform_service.domain.repository.FeatureModuleRepository;
import com.personalmanager.platform_service.exception.DuplicateFeatureModuleException;
import com.personalmanager.platform_service.exception.FeatureModuleNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeatureModuleServiceTest {

    @Mock
    private FeatureModuleRepository repository;

    private FeatureModuleService service;

    @BeforeEach
    void setUp() {
        service = new FeatureModuleService(repository);
    }

    @Test
    void createShouldNormalizeCodeAndSaveFeature() {
        when(repository.existsByCode("PASSWORD_MANAGER"))
                .thenReturn(false);

        when(repository.save(any(FeatureModule.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FeatureModule result = service.create(
                " password_manager ",
                " Password Manager ",
                " Secure credential storage ");

        assertEquals("PASSWORD_MANAGER", result.getCode());
        assertEquals("Password Manager", result.getDisplayName());
        assertEquals("Secure credential storage", result.getDescription());
        assertTrue(result.isEnabled());

        verify(repository).save(any(FeatureModule.class));
    }

    @Test
    void createShouldRejectDuplicateCode() {
        when(repository.existsByCode("PASSWORD_MANAGER"))
                .thenReturn(true);

        assertThrows(
                DuplicateFeatureModuleException.class,
                () -> service.create(
                        "password_manager",
                        "Password Manager",
                        null));

        verify(repository, never()).save(any());
    }

    @Test
    void createShouldRejectInvalidCode() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.create(
                        "password-manager",
                        "Password Manager",
                        null));

        verifyNoInteractions(repository);
    }

    @Test
    void getByIdShouldThrowWhenFeatureDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(
                FeatureModuleNotFoundException.class,
                () -> service.getById(id));
    }

    @Test
    void disableShouldUpdateFeatureState() {
        UUID id = UUID.randomUUID();
        FeatureModule module = new FeatureModule(
                "PASSWORD_MANAGER",
                "Password Manager",
                null);

        when(repository.findById(id)).thenReturn(Optional.of(module));

        FeatureModule result = service.disable(id);

        assertFalse(result.isEnabled());
    }
}