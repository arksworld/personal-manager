package com.personalmanager.platform_service.api;


import com.personalmanager.platform_service.api.controller.DashboardController;
import com.personalmanager.platform_service.api.dto.DashboardFeatureResponse;
import com.personalmanager.platform_service.api.dto.DashboardResponse;
import com.personalmanager.platform_service.config.SecurityConfig;
import com.personalmanager.platform_service.service.DashboardService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@Import(SecurityConfig.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @Test
    void shouldReturnDashboardForAuthenticatedUser() throws Exception {
        String subject = "test-user-001";

        when(dashboardService.getDashboard(subject))
                .thenReturn(new DashboardResponse(
                        subject,
                        List.of(
                                new DashboardFeatureResponse(
                                        "PASSWORD_MANAGER",
                                        "Password Manager",
                                        "Secure credential storage",
                                        true
                                )
                        )
                ));

        mockMvc.perform(get("/dashboard").with(user(subject)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userSubject").value(subject))
                .andExpect(jsonPath("$.features.length()").value(1))
                .andExpect(jsonPath("$.features[0].code")
                        .value("PASSWORD_MANAGER"))
                .andExpect(jsonPath("$.features[0].displayName")
                        .value("Password Manager"))
                .andExpect(jsonPath("$.features[0].enabled").value(true));

        verify(dashboardService).getDashboard(subject);
    }

    @Test
    void shouldReturnEmptyFeatureListWhenUserHasNoEntitlements()
            throws Exception {
        String subject = "new-user-001";

        when(dashboardService.getDashboard(subject))
                .thenReturn(new DashboardResponse(subject, List.of()));

        mockMvc.perform(get("/dashboard").with(user(subject)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userSubject").value(subject))
                .andExpect(jsonPath("$.features.length()").value(0));
    }
}
