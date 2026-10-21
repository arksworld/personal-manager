package com.personalmanager.platform_service.service;


import com.personalmanager.platform_service.domain.model.FeatureModule;
import com.personalmanager.platform_service.domain.repository.FeatureModuleRepository;
import com.personalmanager.platform_service.exception.DuplicateFeatureModuleException;
import com.personalmanager.platform_service.exception.FeatureModuleNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class FeatureModuleService {

    private final FeatureModuleRepository repository;

    public FeatureModuleService(FeatureModuleRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public FeatureModule create(
            String code,
            String displayName,
            String description) {

        String normalizedCode = normalizeCode(code);
        String normalizedName = normalizeRequired(displayName, "Display name");

        if (repository.existsByCode(normalizedCode)) {
            throw new DuplicateFeatureModuleException(normalizedCode);
        }

        FeatureModule module = new FeatureModule(
                normalizedCode,
                normalizedName,
                normalizeOptional(description));

        return repository.save(module);
    }

    public FeatureModule getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new FeatureModuleNotFoundException(id));
    }

    public FeatureModule getByCode(String code) {
        String normalizedCode = normalizeCode(code);

        return repository.findByCode(normalizedCode)
                .orElseThrow(
                        () -> new FeatureModuleNotFoundException(normalizedCode));
    }

    public List<FeatureModule> getAll() {
        return repository.findAll();
    }

    public List<FeatureModule> getEnabled() {
        return repository.findAllByEnabledTrueOrderByDisplayNameAsc();
    }

    @Transactional
    public FeatureModule updateDetails(
            UUID id,
            String displayName,
            String description) {

        FeatureModule module = getById(id);

        module.updateDetails(
                normalizeRequired(displayName, "Display name"),
                normalizeOptional(description));

        return module;
    }

    @Transactional
    public FeatureModule enable(UUID id) {
        FeatureModule module = getById(id);
        module.enable();
        return module;
    }

    @Transactional
    public FeatureModule disable(UUID id) {
        FeatureModule module = getById(id);
        module.disable();
        return module;
    }

    private String normalizeCode(String code) {
        String normalized = normalizeRequired(code, "Feature code")
                .toUpperCase(Locale.ROOT);

        if (!normalized.matches("[A-Z][A-Z0-9_]*")) {
            throw new IllegalArgumentException(
                    "Feature code must start with a letter and contain "
                            + "only letters, digits, and underscores");
        }

        if (normalized.length() > 100) {
            throw new IllegalArgumentException(
                    "Feature code must not exceed 100 characters");
        }

        return normalized;
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be blank");
        }

        return value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}