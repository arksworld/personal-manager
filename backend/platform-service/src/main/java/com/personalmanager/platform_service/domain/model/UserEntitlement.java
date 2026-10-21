package com.personalmanager.platform_service.domain.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "user_entitlement",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_entitlement_user_feature",
                columnNames = {"user_subject", "feature_module_id"}
        )
)
public class UserEntitlement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_subject", nullable = false, length = 255)
    private String userSubject;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feature_module_id", nullable = false)
    private FeatureModule featureModule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EntitlementStatus status = EntitlementStatus.ACTIVE;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "valid_until")
    private Instant validUntil;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserEntitlement() {
        // Required by JPA.
    }

    public UserEntitlement(
            String userSubject,
            FeatureModule featureModule,
            Instant validFrom,
            Instant validUntil) {

        this.userSubject = userSubject;
        this.featureModule = featureModule;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.status = EntitlementStatus.ACTIVE;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }
        if (validFrom == null) {
            validFrom = now;
        }

        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public boolean isEffectiveAt(Instant instant) {
        return status == EntitlementStatus.ACTIVE
                && !validFrom.isAfter(instant)
                && (validUntil == null || validUntil.isAfter(instant))
                && featureModule.isEnabled();
    }

    public void suspend() {
        status = EntitlementStatus.SUSPENDED;
    }

    public void revoke() {
        status = EntitlementStatus.REVOKED;
    }

    public void reactivate() {
        status = EntitlementStatus.ACTIVE;
    }

    public UUID getId() {
        return id;
    }

    public String getUserSubject() {
        return userSubject;
    }

    public FeatureModule getFeatureModule() {
        return featureModule;
    }

    public EntitlementStatus getStatus() {
        return status;
    }

    public Instant getValidFrom() {
        return validFrom;
    }

    public Instant getValidUntil() {
        return validUntil;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}