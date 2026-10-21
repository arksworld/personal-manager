package com.personalmanager.platform_service.api.dto;

import com.personalmanager.platform_service.domain.model.EntitlementStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateEntitlementStatusRequest(
        @NotNull EntitlementStatus status
) {
}