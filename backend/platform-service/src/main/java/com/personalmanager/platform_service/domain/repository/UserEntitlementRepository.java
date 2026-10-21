package com.personalmanager.platform_service.domain.repository;

import com.personalmanager.platform_service.domain.model.EntitlementStatus;
import com.personalmanager.platform_service.domain.model.UserEntitlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserEntitlementRepository
        extends JpaRepository<UserEntitlement, UUID> {

    List<UserEntitlement> findAllByUserSubject(String userSubject);

    Optional<UserEntitlement> findByUserSubjectAndFeatureModule_Code(
            String userSubject,
            String code);

    boolean existsByUserSubjectAndFeatureModule_Id(
            String userSubject,
            UUID featureModuleId);

    List<UserEntitlement> findAllByUserSubjectAndStatus(
            String userSubject,
            EntitlementStatus status);
}