package com.personalmanager.platform_service.api.controller;

import com.personalmanager.platform_service.api.dto.CreateFeatureModuleRequest;
import com.personalmanager.platform_service.api.dto.FeatureEnabledRequest;
import com.personalmanager.platform_service.api.dto.FeatureModuleResponse;
import com.personalmanager.platform_service.api.dto.UpdateFeatureModuleRequest;
import com.personalmanager.platform_service.service.FeatureModuleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/features")
public class FeatureModuleController {

    private final FeatureModuleService service;

    public FeatureModuleController(FeatureModuleService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<FeatureModuleResponse> create(
            @Valid @RequestBody CreateFeatureModuleRequest request) {

        var module = service.create(
                request.code(),
                request.displayName(),
                request.description());

        var response = FeatureModuleResponse.from(module);

        var location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public List<FeatureModuleResponse> getAll(
            @RequestParam(defaultValue = "false") boolean enabledOnly) {

        var modules = enabledOnly
                ? service.getEnabled()
                : service.getAll();

        return modules.stream()
                .map(FeatureModuleResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public FeatureModuleResponse getById(@PathVariable UUID id) {
        return FeatureModuleResponse.from(service.getById(id));
    }

    @PutMapping("/{id}")
    public FeatureModuleResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFeatureModuleRequest request) {

        var module = service.updateDetails(
                id,
                request.displayName(),
                request.description());

        return FeatureModuleResponse.from(module);
    }

    @PatchMapping("/{id}/enabled")
    public FeatureModuleResponse setEnabled(
            @PathVariable UUID id,
            @Valid @RequestBody FeatureEnabledRequest request) {

        var module = Boolean.TRUE.equals(request.enabled())
                ? service.enable(id)
                : service.disable(id);

        return FeatureModuleResponse.from(module);
    }
}