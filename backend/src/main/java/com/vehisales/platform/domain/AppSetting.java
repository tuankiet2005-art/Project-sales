package com.vehisales.platform.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "app_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppSetting {

    @Id
    @Column(name = "setting_key", length = 80)
    private String key;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;
}
