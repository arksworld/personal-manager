package com.personalmanager.platform_service.exception;


public class DuplicateFeatureModuleException extends RuntimeException {

    public DuplicateFeatureModuleException(String code) {
        super("Feature module already exists with code: " + code);
    }
}