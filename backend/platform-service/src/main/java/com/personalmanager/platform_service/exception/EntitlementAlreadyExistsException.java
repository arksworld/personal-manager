package com.personalmanager.platform_service.exception;

public class EntitlementAlreadyExistsException extends RuntimeException {

    public EntitlementAlreadyExistsException(String userSubject, String code) {
        super("Entitlement already exists for user and feature: "
                + userSubject + ", " + code);
    }
}