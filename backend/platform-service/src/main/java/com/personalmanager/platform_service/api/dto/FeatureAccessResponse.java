package com.personalmanager.platform_service.api.dto;

public record FeatureAccessResponse(
        String userSubject,
        String featureCode,
        boolean hasAccess
) {
}