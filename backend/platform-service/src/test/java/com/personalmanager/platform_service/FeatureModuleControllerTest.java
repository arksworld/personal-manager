package com.personalmanager.platform_service;

import com.personalmanager.platform_service.api.controller.FeatureModuleController;
import com.personalmanager.platform_service.api.dto.CreateFeatureModuleRequest;
import com.personalmanager.platform_service.api.exception.GlobalExceptionHandler;
import com.personalmanager.platform_service.config.SecurityConfig;
import com.personalmanager.platform_service.domain.model.FeatureModule;
import com.personalmanager.platform_service.service.FeatureModuleService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FeatureModuleController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class FeatureModuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FeatureModuleService service;

    @Test
    void getFeaturesShouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/features"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    @Test
    void getFeaturesShouldReturnSuccessForAuthenticatedUser()
            throws Exception {

        when(service.getAll()).thenReturn(java.util.List.of());

        mockMvc.perform(get("/features")
                        .with(user("test-user")))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(service).getAll();
    }

    @Test
    void createFeatureShouldReturn201AndLocation() throws Exception {
        UUID id = UUID.randomUUID();

        FeatureModule module = new FeatureModule(
                "PASSWORD_MANAGER",
                "Password Manager",
                "Secure credential storage");

        ReflectionTestUtils.setField(module, "id", id);

        when(service.create(
                eq("PASSWORD_MANAGER"),
                eq("Password Manager"),
                eq("Secure credential storage")))
                .thenReturn(module);

        mockMvc.perform(post("/features")
                        .with(user("test-user"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "PASSWORD_MANAGER",
                                  "displayName": "Password Manager",
                                  "description": "Secure credential storage"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        org.hamcrest.Matchers.endsWith("/" + id)))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.code").value("PASSWORD_MANAGER"))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void createFeatureShouldRejectBlankCode() throws Exception {
        mockMvc.perform(post("/features")
                        .with(user("test-user"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": " ",
                                  "displayName": "Password Manager",
                                  "description": "Test"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }
}