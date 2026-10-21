package com.personalmanager.platform_service.api.dto;

import jakarta.validation.constraints.NotNull;

public record FeatureEnabledRequest(
        @NotNull Boolean enabled
) {
}