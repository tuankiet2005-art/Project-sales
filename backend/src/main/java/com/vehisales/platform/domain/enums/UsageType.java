package com.vehisales.platform.domain.enums;

public enum UsageType {
    PRIVATE,
    COMMERCIAL;

    public static UsageType from(String raw) {
        if (raw == null || raw.isBlank()) {
            return PRIVATE;
        }
        try {
            return UsageType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return PRIVATE;
        }
    }

    public boolean isCommercial() {
        return this == COMMERCIAL;
    }
}
