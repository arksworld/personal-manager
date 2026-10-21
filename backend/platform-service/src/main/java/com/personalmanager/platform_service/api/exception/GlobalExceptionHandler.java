package com.personalmanager.platform_service.api.exception;

import com.personalmanager.platform_service.exception.DuplicateFeatureModuleException;
import com.personalmanager.platform_service.exception.EntitlementAlreadyExistsException;
import com.personalmanager.platform_service.exception.EntitlementNotFoundException;
import com.personalmanager.platform_service.exception.FeatureModuleNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FeatureModuleNotFoundException.class)
    public ProblemDetail handleNotFound(
            FeatureModuleNotFoundException exception) {

        return problem(
                HttpStatus.NOT_FOUND,
                "Feature module not found",
                exception.getMessage(),
                "/errors/feature-not-found");
    }

    @ExceptionHandler(DuplicateFeatureModuleException.class)
    public ProblemDetail handleDuplicate(
            DuplicateFeatureModuleException exception) {

        return problem(
                HttpStatus.CONFLICT,
                "Duplicate feature module",
                exception.getMessage(),
                "/errors/duplicate-feature");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(
            MethodArgumentNotValidException exception) {

        String details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": "
                        + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return problem(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                details,
                "/errors/validation");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(
            IllegalArgumentException exception) {

        return problem(
                HttpStatus.BAD_REQUEST,
                "Invalid request",
                exception.getMessage(),
                "/errors/invalid-request");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(
            DataIntegrityViolationException exception) {

        return problem(
                HttpStatus.CONFLICT,
                "Data conflict",
                "The operation conflicts with existing data.",
                "/errors/data-conflict");
    }

    private ProblemDetail problem(
            HttpStatus status,
            String title,
            String detail,
            String type) {

        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(status, detail);

        problem.setTitle(title);
        problem.setType(URI.create(type));

        return problem;
    }

    @ExceptionHandler(EntitlementNotFoundException.class)
    public ProblemDetail handleEntitlementNotFound(
            EntitlementNotFoundException exception) {

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(EntitlementAlreadyExistsException.class)
    public ProblemDetail handleEntitlementAlreadyExists(
            EntitlementAlreadyExistsException exception) {

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, exception.getMessage());
    }
}