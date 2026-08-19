package com.vehisales.platform.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

final class DatabaseUrls {

    record Parsed(String jdbcUrl, String username, String password) {
    }

    private DatabaseUrls() {
    }

    static Parsed parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("DATABASE_URL is missing");
        }
        String normalized = raw.trim();
        if (normalized.startsWith("jdbc:")) {
            normalized = normalized.substring("jdbc:".length());
        }
        if (normalized.startsWith("postgres://")) {
            normalized = "postgresql://" + normalized.substring("postgres://".length());
        }
        if (!normalized.startsWith("postgresql://")) {
            throw new IllegalArgumentException("DATABASE_URL must be a PostgreSQL URL");
        }

        URI uri = URI.create(normalized);
        String username = null;
        String password = null;
        if (uri.getUserInfo() != null) {
            String userInfo = uri.getUserInfo();
            int colon = userInfo.indexOf(':');
            if (colon >= 0) {
                username = decode(userInfo.substring(0, colon));
                password = decode(userInfo.substring(colon + 1));
            } else {
                username = decode(userInfo);
            }
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("DATABASE_URL is missing a host");
        }
        int port = uri.getPort() > 0 ? uri.getPort() : 5432;
        String path = uri.getPath() == null || uri.getPath().isBlank() ? "/neondb" : uri.getPath();
        String query = uri.getQuery();
        if (query == null || query.isBlank()) {
            query = "sslmode=require";
        } else if (!query.contains("sslmode=")) {
            query = query + "&sslmode=require";
        }

        return new Parsed("jdbc:postgresql://" + host + ":" + port + path + "?" + query, username, password);
    }

    static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
