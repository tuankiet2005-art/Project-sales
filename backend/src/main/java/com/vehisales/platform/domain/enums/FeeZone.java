package com.vehisales.platform.domain.enums;

/**
 * Location grouping used by fee rules so plate/registration amounts
 * can be shared across many provinces without duplicating rows.
 */
public enum FeeZone {
    SPECIAL,
    MAJOR,
    STANDARD
}
