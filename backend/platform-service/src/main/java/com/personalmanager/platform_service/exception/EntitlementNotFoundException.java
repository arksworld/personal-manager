package com.personalmanager.platform_service.exception;

import java.util.UUID;

public class EntitlementNotFoundException extends RuntimeException {

    public EntitlementNotFoundException(UUID id) {
        super("Entitlement not found: " + id);
    }
}