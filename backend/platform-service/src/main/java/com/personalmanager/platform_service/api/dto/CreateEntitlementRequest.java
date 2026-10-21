package com.personalmanager.platform_service.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CreateEntitlementRequest(
        @NotBlank String userSubject,
        @NotBlank String featureCode,
        @NotNull Instant validFrom,
        Instant validUntil
) {
}
