package com.personalmanager.platform_service.api.controller;


import com.personalmanager.platform_service.api.dto.DashboardResponse;
import com.personalmanager.platform_service.service.DashboardService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public DashboardResponse getDashboard(Principal principal) {
        return dashboardService.getDashboard(principal.getName());
    }
}
