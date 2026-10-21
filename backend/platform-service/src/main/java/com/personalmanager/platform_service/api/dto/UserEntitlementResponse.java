package com.personalmanager.platform_service.api.dto;

import com.personalmanager.platform_service.domain.model.UserEntitlement;

import java.time.Instant;
import java.util.UUID;

public record UserEntitlementResponse(
        UUID id,
        String userSubject,
        UUID featureModuleId,
        String featureCode,
        String featureDisplayName,
        String status,
        Instant validFrom,
        Instant validUntil,
        Instant createdAt,
        Instant updatedAt,
        boolean effective
) {
    public static UserEntitlementResponse from(
            UserEntitlement entitlement,
            Instant evaluatedAt
    ) {
        var feature = entitlement.getFeatureModule();

        return new UserEntitlementResponse(
                entitlement.getId(),
                entitlement.getUserSubject(),
                feature.getId(),
                feature.getCode(),
                feature.getDisplayName(),
                entitlement.getStatus().name(),
                entitlement.getValidFrom(),
                entitlement.getValidUntil(),
                entitlement.getCreatedAt(),
                entitlement.getUpdatedAt(),
                entitlement.isEffectiveAt(evaluatedAt)
        );
    }
}
