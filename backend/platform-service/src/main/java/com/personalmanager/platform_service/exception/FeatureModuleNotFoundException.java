package com.personalmanager.platform_service.exception;

import java.util.UUID;

public class FeatureModuleNotFoundException extends RuntimeException {

    public FeatureModuleNotFoundException(UUID id) {
        super("Feature module not found: " + id);
    }

    public FeatureModuleNotFoundException(String code) {
        super("Feature module not found with code: " + code);
    }
}