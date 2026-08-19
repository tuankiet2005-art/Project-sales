package com.vehisales.platform.api.dto.admin;

public record DealerRecord(
        Long id,
        String brandCode,
        String name,
        String address,
        String market,
        Boolean active
) {
}
