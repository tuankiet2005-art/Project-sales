package com.vehisales.platform.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@Profile("postgres")
public class PostgresDataSourceConfig {

    @Bean
    public DataSource dataSource(
            @Value("${DATABASE_URL}") String databaseUrl,
            @Value("${DATABASE_USERNAME:}") String username,
            @Value("${DATABASE_PASSWORD:}") String password
    ) {
        DatabaseUrls.Parsed parsed = DatabaseUrls.parse(databaseUrl);
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(parsed.jdbcUrl());
        dataSource.setUsername(DatabaseUrls.firstNonBlank(username, parsed.username()));
        dataSource.setPassword(DatabaseUrls.firstNonBlank(password, parsed.password()));
        dataSource.setDriverClassName("org.postgresql.Driver");
        return dataSource;
    }
}
