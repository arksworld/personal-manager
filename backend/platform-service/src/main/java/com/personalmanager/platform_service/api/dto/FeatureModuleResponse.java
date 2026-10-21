package com.personalmanager.platform_service.api.dto;

import com.personalmanager.platform_service.domain.model.FeatureModule;

import java.time.Instant;
import java.util.UUID;

public record FeatureModuleResponse(
        UUID id,
        String code,
        String displayName,
        String description,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
    public static FeatureModuleResponse from(FeatureModule module) {
        return new FeatureModuleResponse(
                module.getId(),
                module.getCode(),
                module.getDisplayName(),
                module.getDescription(),
                module.isEnabled(),
                module.getCreatedAt(),
                module.getUpdatedAt()
        );
    }
}