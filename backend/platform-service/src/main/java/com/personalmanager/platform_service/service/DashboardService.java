package com.personalmanager.platform_service.service;


import com.personalmanager.platform_service.api.dto.DashboardFeatureResponse;
import com.personalmanager.platform_service.api.dto.DashboardResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final UserEntitlementService entitlementService;

    public DashboardService(UserEntitlementService entitlementService) {
        this.entitlementService = entitlementService;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(String userSubject) {
        if (userSubject == null || userSubject.isBlank()) {
            throw new IllegalArgumentException(
                    "Authenticated user subject must not be blank");
        }

        var features = entitlementService
                .getEffectiveForUser(userSubject)
                .stream()
                .map(entitlement ->
                        DashboardFeatureResponse.from(
                                entitlement.getFeatureModule()))
                .toList();

        return new DashboardResponse(userSubject, features);
    }
}
