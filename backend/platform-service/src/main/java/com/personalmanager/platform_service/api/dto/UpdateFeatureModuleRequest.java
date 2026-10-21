package com.personalmanager.platform_service.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateFeatureModuleRequest(
        @NotBlank
        @Size(max = 150)
        String displayName,

        @Size(max = 2000)
        String description
) {
}