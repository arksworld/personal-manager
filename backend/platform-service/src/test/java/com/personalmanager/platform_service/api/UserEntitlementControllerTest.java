package com.personalmanager.platform_service.api;


import com.personalmanager.platform_service.api.controller.UserEntitlementController;
import com.personalmanager.platform_service.api.dto.CreateEntitlementRequest;
import com.personalmanager.platform_service.api.dto.UpdateEntitlementStatusRequest;
import com.personalmanager.platform_service.api.exception.GlobalExceptionHandler;
import com.personalmanager.platform_service.domain.model.EntitlementStatus;
import com.personalmanager.platform_service.domain.model.FeatureModule;
import com.personalmanager.platform_service.domain.model.UserEntitlement;
import com.personalmanager.platform_service.exception.EntitlementAlreadyExistsException;
import com.personalmanager.platform_service.exception.EntitlementNotFoundException;
import com.personalmanager.platform_service.service.UserEntitlementService;
import com.personalmanager.platform_service.config.SecurityConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;


import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserEntitlementController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class UserEntitlementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserEntitlementService entitlementService;

    private FeatureModule feature;
    private UserEntitlement entitlement;
    private UUID entitlementId;
    private UUID featureId;

    private static final String USER_SUBJECT = "test-user-001";
    private static final String FEATURE_CODE = "PASSWORD_MANAGER";

    @BeforeEach
    void setUp() {
        feature = new FeatureModule(
                FEATURE_CODE,
                "Password Manager",
                "Secure credential storage"
        );

        featureId = UUID.randomUUID();
        ReflectionTestUtils.setField(feature, "id", featureId);

        entitlementId = UUID.randomUUID();

        entitlement = new UserEntitlement(
                USER_SUBJECT,
                feature,
                Instant.parse("2026-01-01T00:00:00Z"),
                null
        );

        ReflectionTestUtils.setField(entitlement, "id", entitlementId);
    }

    @Test
    void grantShouldReturnCreatedEntitlement() throws Exception {
        when(entitlementService.grant(
                USER_SUBJECT,
                FEATURE_CODE,
                Instant.parse("2026-01-01T00:00:00Z"),
                null
        )).thenReturn(entitlement);

        CreateEntitlementRequest request = new CreateEntitlementRequest(
                USER_SUBJECT,
                FEATURE_CODE,
                Instant.parse("2026-01-01T00:00:00Z"),
                null
        );

        mockMvc.perform(post("/entitlements")
                        .with(user("admin"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(entitlementId.toString()))
                .andExpect(jsonPath("$.userSubject").value(USER_SUBJECT))
                .andExpect(jsonPath("$.featureModuleId").value(featureId.toString()))
                .andExpect(jsonPath("$.featureCode").value(FEATURE_CODE))
                .andExpect(jsonPath("$.featureDisplayName").value("Password Manager"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.effective").value(true));

        verify(entitlementService).grant(
                USER_SUBJECT,
                FEATURE_CODE,
                Instant.parse("2026-01-01T00:00:00Z"),
                null
        );
    }

    @Test
    void grantShouldRejectBlankUserSubject() throws Exception {
        String request = """
                {
                  "userSubject": " ",
                  "featureCode": "PASSWORD_MANAGER",
                  "validFrom": "2026-01-01T00:00:00Z",
                  "validUntil": null
                }
                """;

        mockMvc.perform(post("/entitlements")
                        .with(user("admin"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(entitlementService);
    }

    @Test
    void getByIdShouldReturnEntitlementAndFeatureDetails() throws Exception {
        when(entitlementService.getById(entitlementId))
                .thenReturn(entitlement);

        mockMvc.perform(get("/entitlements/{id}", entitlementId)
                        .with(user("test-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(entitlementId.toString()))
                .andExpect(jsonPath("$.featureCode").value(FEATURE_CODE))
                .andExpect(jsonPath("$.featureDisplayName").value("Password Manager"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.effective").value(true));
    }

    @Test
    void getByIdShouldReturnNotFoundWhenEntitlementDoesNotExist()
            throws Exception {
        when(entitlementService.getById(entitlementId))
                .thenThrow(new EntitlementNotFoundException(entitlementId));

        mockMvc.perform(get("/entitlements/{id}", entitlementId)
                        .with(user("test-user")))
                .andExpect(status().isNotFound());
    }

    @Test
    void getForUserShouldReturnEntitlements() throws Exception {
        when(entitlementService.getForUser(USER_SUBJECT))
                .thenReturn(List.of(entitlement));

        mockMvc.perform(get("/entitlements")
                        .param("userSubject", USER_SUBJECT)
                        .with(user("test-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(entitlementId.toString()))
                .andExpect(jsonPath("$[0].featureCode").value(FEATURE_CODE))
                .andExpect(jsonPath("$[0].effective").value(true));

        verify(entitlementService).getForUser(USER_SUBJECT);
    }

    @Test
    void getForUserShouldReturnEffectiveEntitlementsWhenRequested()
            throws Exception {
        when(entitlementService.getEffectiveForUser(USER_SUBJECT))
                .thenReturn(List.of(entitlement));

        mockMvc.perform(get("/entitlements")
                        .param("userSubject", USER_SUBJECT)
                        .param("effectiveOnly", "true")
                        .with(user("test-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].effective").value(true));

        verify(entitlementService).getEffectiveForUser(USER_SUBJECT);
        verify(entitlementService, never()).getForUser(anyString());
    }

    @Test
    void checkAccessShouldReturnAccessResult() throws Exception {
        when(entitlementService.hasAccess(USER_SUBJECT, FEATURE_CODE))
                .thenReturn(true);

        mockMvc.perform(get("/entitlements/access")
                        .param("userSubject", USER_SUBJECT)
                        .param("featureCode", FEATURE_CODE)
                        .with(user("test-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userSubject").value(USER_SUBJECT))
                .andExpect(jsonPath("$.featureCode").value(FEATURE_CODE))
                .andExpect(jsonPath("$.hasAccess").value(true));
    }

    @Test
    void suspendShouldReturnUpdatedStatus() throws Exception {
        entitlement.suspend();
        when(entitlementService.suspend(entitlementId))
                .thenReturn(entitlement);

        mockMvc.perform(patch("/entitlements/{id}/status", entitlementId)
                        .with(user("admin"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"SUSPENDED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUSPENDED"))
                .andExpect(jsonPath("$.effective").value(false));
    }

    @Test
    void revokeShouldReturnUpdatedStatus() throws Exception {
        entitlement.revoke();
        when(entitlementService.revoke(entitlementId))
                .thenReturn(entitlement);

        mockMvc.perform(patch("/entitlements/{id}/status", entitlementId)
                        .with(user("admin"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"REVOKED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVOKED"))
                .andExpect(jsonPath("$.effective").value(false));
    }

    @Test
    void reactivateShouldReturnActiveStatus() throws Exception {
        entitlement.suspend();
        entitlement.reactivate();

        when(entitlementService.reactivate(entitlementId))
                .thenReturn(entitlement);

        mockMvc.perform(patch("/entitlements/{id}/status", entitlementId)
                        .with(user("admin"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"ACTIVE"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.effective").value(true));
    }

    @Test
    void updateStatusShouldRejectNullStatus() throws Exception {
        mockMvc.perform(patch("/entitlements/{id}/status", entitlementId)
                        .with(user("admin"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":null}
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(entitlementService);
    }

    @Test
    void grantShouldReturnConflictForDuplicateEntitlement()
            throws Exception {
        when(entitlementService.grant(
                USER_SUBJECT,
                FEATURE_CODE,
                Instant.parse("2026-01-01T00:00:00Z"),
                null
        )).thenThrow(new EntitlementAlreadyExistsException(
                USER_SUBJECT, FEATURE_CODE));

        mockMvc.perform(post("/entitlements")
                        .with(user("admin"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userSubject":"test-user-001",
                                  "featureCode":"PASSWORD_MANAGER",
                                  "validFrom":"2026-01-01T00:00:00Z",
                                  "validUntil":null
                                }
                                """))
                .andExpect(status().isConflict());
    }
}
