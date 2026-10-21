package com.personalmanager.platform_service.domain.repository;

import com.personalmanager.platform_service.domain.model.EntitlementStatus;
import com.personalmanager.platform_service.domain.model.UserEntitlement;
import jakarta.persistence.Id;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserEntitlementRepository
        extends JpaRepository<UserEntitlement, UUID> {



    @EntityGraph(attributePaths = "featureModule")
    @Override
    Optional<UserEntitlement> findById(UUID id);

    @EntityGraph(attributePaths = "featureModule")
    List<UserEntitlement> findAllByUserSubject(String userSubject);

    @EntityGraph(attributePaths = "featureModule")
    Optional<UserEntitlement> findByUserSubjectAndFeatureModule_Code(
            String userSubject,
            String code);

    boolean existsByUserSubjectAndFeatureModule_Id(
            String userSubject,
            UUID featureModuleId);

    @EntityGraph(attributePaths = "featureModule")
    List<UserEntitlement> findAllByUserSubjectAndStatus(
            String userSubject,
            EntitlementStatus status);
}