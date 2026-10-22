package com.personalmanager.platform_service.api.dto;

import java.util.List;

public record DashboardResponse(
        String userSubject,
        List<DashboardFeatureResponse> features
) {
}