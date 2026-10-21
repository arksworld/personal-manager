package com.personalmanager.platform_service.domain.repository;

import com.personalmanager.platform_service.domain.model.FeatureModule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeatureModuleRepository
        extends JpaRepository<FeatureModule, UUID> {

    Optional<FeatureModule> findByCode(String code);

    List<FeatureModule> findAllByEnabledTrueOrderByDisplayNameAsc();

    boolean existsByCode(String code);
}