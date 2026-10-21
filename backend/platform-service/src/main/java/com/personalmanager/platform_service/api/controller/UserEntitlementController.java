package com.personalmanager.platform_service.api.controller;

import com.personalmanager.platform_service.api.dto.CreateEntitlementRequest;
import com.personalmanager.platform_service.api.dto.FeatureAccessResponse;
import com.personalmanager.platform_service.api.dto.UpdateEntitlementStatusRequest;
import com.personalmanager.platform_service.api.dto.UserEntitlementResponse;
import com.personalmanager.platform_service.domain.model.EntitlementStatus;
import com.personalmanager.platform_service.service.UserEntitlementService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/entitlements")
public class UserEntitlementController {

    private final UserEntitlementService entitlementService;

    public UserEntitlementController(
            UserEntitlementService entitlementService) {
        this.entitlementService = entitlementService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserEntitlementResponse grant(
            @Valid @RequestBody CreateEntitlementRequest request) {

        var entitlement = entitlementService.grant(
                request.userSubject(),
                request.featureCode(),
                request.validFrom(),
                request.validUntil()
        );

        return UserEntitlementResponse.from(entitlement, Instant.now());
    }

    @GetMapping("/{id}")
    public UserEntitlementResponse getById(@PathVariable UUID id) {
        return UserEntitlementResponse.from(
                entitlementService.getById(id),
                Instant.now()
        );
    }

    @GetMapping
    public List<UserEntitlementResponse> getForUser(
            @RequestParam String userSubject,
            @RequestParam(defaultValue = "false") boolean effectiveOnly) {

        Instant now = Instant.now();

        var entitlements = effectiveOnly
                ? entitlementService.getEffectiveForUser(userSubject)
                : entitlementService.getForUser(userSubject);

        return entitlements.stream()
                .map(entitlement ->
                        UserEntitlementResponse.from(entitlement, now))
                .toList();
    }

    @GetMapping("/access")
    public FeatureAccessResponse checkAccess(
            @RequestParam String userSubject,
            @RequestParam String featureCode) {

        boolean hasAccess =
                entitlementService.hasAccess(userSubject, featureCode);

        return new FeatureAccessResponse(
                userSubject, featureCode, hasAccess);
    }

    @PatchMapping("/{id}/status")
    public UserEntitlementResponse updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEntitlementStatusRequest request) {

        var entitlement = switch (request.status()) {
            case ACTIVE -> entitlementService.reactivate(id);
            case SUSPENDED -> entitlementService.suspend(id);
            case REVOKED -> entitlementService.revoke(id);
        };

        return UserEntitlementResponse.from(entitlement, Instant.now());
    }
}
