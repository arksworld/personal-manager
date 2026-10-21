package com.personalmanager.platform_service.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateFeatureModuleRequest(
        @NotBlank
        @Size(max = 100)
        String code,

        @NotBlank
        @Size(max = 150)
        String displayName,

        @Size(max = 2000)
        String description
) {
}