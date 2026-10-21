package com.personalmanager.platform_service.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/status")
public class StatusController {

    @GetMapping
    public Map<String, String> status() {
        return Map.of(
                "service", "platform-service",
                "status", "UP"
        );
    }
}