package com.vehisales.platform.repository;

import com.vehisales.platform.domain.AppSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppSettingRepository extends JpaRepository<AppSetting, String> {
}
