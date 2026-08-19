package com.vehisales.platform.service;

import com.vehisales.platform.config.AdminAuthProperties;
import com.vehisales.platform.exception.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdminAuthServiceTest {

    private AdminAuthService service;

    @BeforeEach
    void setUp() {
        AdminAuthProperties properties = new AdminAuthProperties();
        properties.setUsername("admin");
        properties.setPassword("Admin!!@");
        properties.setTokenSecret("test-secret");
        service = new AdminAuthService(properties);
    }

    @Test
    void loginReturnsBearerTokenForHardAccount() {
        String token = service.login("admin", "Admin!!@");
        assertThat(token).isNotBlank();
        assertThat(service.isAuthorized("Bearer " + token)).isTrue();
    }

    @Test
    void loginRejectsWrongPassword() {
        assertThatThrownBy(() -> service.login("admin", "wrong"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void missingHeaderIsRejected() {
        assertThat(service.isAuthorized(null)).isFalse();
        assertThat(service.isAuthorized("Bearer nope")).isFalse();
    }
}
