package com.personalmanager.platform_service.api.dto;
import com.personalmanager.platform_service.domain.model.FeatureModule;

public record DashboardFeatureResponse(
        String code,
        String displayName,
        String description,
        boolean enabled
) {
    public static DashboardFeatureResponse from(FeatureModule feature) {
        return new DashboardFeatureResponse(
                feature.getCode(),
                feature.getDisplayName(),
                feature.getDescription(),
                feature.isEnabled()
        );
    }
}