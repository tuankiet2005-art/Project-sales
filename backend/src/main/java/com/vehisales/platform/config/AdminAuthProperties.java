package com.vehisales.platform.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.admin")
public class AdminAuthProperties {

    private String username = "admin";
    private String password = "Admin!!@";
    private String tokenSecret = "onroad-admin-hmac";
}
