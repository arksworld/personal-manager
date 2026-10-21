package com.personalmanager.platform_service.service;

import com.personalmanager.platform_service.domain.model.FeatureModule;
import com.personalmanager.platform_service.domain.model.UserEntitlement;
import com.personalmanager.platform_service.domain.repository.FeatureModuleRepository;
import com.personalmanager.platform_service.domain.repository.UserEntitlementRepository;
import com.personalmanager.platform_service.exception.EntitlementAlreadyExistsException;
import com.personalmanager.platform_service.exception.EntitlementNotFoundException;
import com.personalmanager.platform_service.exception.FeatureModuleNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserEntitlementService {

    private final UserEntitlementRepository entitlementRepository;
    private final FeatureModuleRepository featureRepository;

    public UserEntitlementService(
            UserEntitlementRepository entitlementRepository,
            FeatureModuleRepository featureRepository) {
        this.entitlementRepository = entitlementRepository;
        this.featureRepository = featureRepository;
    }


    @Transactional
    public UserEntitlement grant(
            String userSubject,
            String featureCode,
            Instant validFrom,
            Instant validUntil) {

        String subject = normalizeSubject(userSubject);
        String code = normalizeCode(featureCode);

        if (validFrom == null) {
            validFrom = Instant.now();
        }

        if (validUntil != null && !validUntil.isAfter(validFrom)) {
            throw new IllegalArgumentException(
                    "validUntil must be later than validFrom");
        }

        FeatureModule feature = featureRepository.findByCode(code)
                .orElseThrow(() -> new FeatureModuleNotFoundException(code));

        if (!feature.isEnabled()) {
            throw new IllegalArgumentException(
                    "Cannot grant an entitlement to a disabled feature");
        }

        if (entitlementRepository
                .existsByUserSubjectAndFeatureModule_Id(subject, feature.getId())) {
            throw new EntitlementAlreadyExistsException(subject, code);
        }

        return entitlementRepository.save(
                new UserEntitlement(subject, feature, validFrom, validUntil));
    }

    public List<UserEntitlement> getForUser(String userSubject) {
        return entitlementRepository.findAllByUserSubject(
                normalizeSubject(userSubject));
    }

    public List<UserEntitlement> getEffectiveForUser(String userSubject) {
        Instant now = Instant.now();

        return getForUser(userSubject).stream()
                .filter(entitlement -> entitlement.isEffectiveAt(now))
                .toList();
    }

    public boolean hasAccess(String userSubject, String featureCode) {
        String subject = normalizeSubject(userSubject);
        String code = normalizeCode(featureCode);

        return entitlementRepository
                .findByUserSubjectAndFeatureModule_Code(subject, code)
                .filter(entitlement -> entitlement.isEffectiveAt(Instant.now()))
                .isPresent();
    }

    @Transactional
    public UserEntitlement suspend(UUID entitlementId) {
        UserEntitlement entitlement = getById(entitlementId);
        entitlement.suspend();
        return entitlement;
    }

    @Transactional
    public UserEntitlement revoke(UUID entitlementId) {
        UserEntitlement entitlement = getById(entitlementId);
        entitlement.revoke();
        return entitlement;
    }

    @Transactional
    public UserEntitlement reactivate(UUID entitlementId) {
        UserEntitlement entitlement = getById(entitlementId);
        entitlement.reactivate();
        return entitlement;
    }

    public UserEntitlement getById(UUID id) {
        UserEntitlement userEntitlement = entitlementRepository.findById(id)
                .orElseThrow(() -> new EntitlementNotFoundException(id));
        return userEntitlement;
    }

    private String normalizeSubject(String subject) {
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("User subject must not be blank");
        }
        return subject.trim();
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Feature code must not be blank");
        }
        return code.trim().toUpperCase(java.util.Locale.ROOT);
    }
}