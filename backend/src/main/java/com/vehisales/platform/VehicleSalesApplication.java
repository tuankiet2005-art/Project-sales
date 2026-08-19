package com.vehisales.platform;

import com.vehisales.platform.config.AdminAuthProperties;
import com.vehisales.platform.config.DealerPolicyProperties;
import com.vehisales.platform.config.FeePolicyProperties;
import com.vehisales.platform.config.LicensePlateRegionsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        AdminAuthProperties.class,
        FeePolicyProperties.class,
        DealerPolicyProperties.class,
        LicensePlateRegionsProperties.class
})
public class VehicleSalesApplication {

    public static void main(String[] args) {
        SpringApplication.run(VehicleSalesApplication.class, args);
    }
}
