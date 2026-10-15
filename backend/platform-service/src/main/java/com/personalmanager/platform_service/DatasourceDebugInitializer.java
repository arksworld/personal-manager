package com.personalmanager.platform_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.time.ZoneId;
import java.util.TimeZone;

public class DatasourceDebugInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger log =
            LoggerFactory.getLogger(DatasourceDebugInitializer.class);

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        Environment env = context.getEnvironment();

        String url = env.getProperty("spring.datasource.url");
        String username = env.getProperty("spring.datasource.username");
        String driver = env.getProperty("spring.datasource.driver-class-name");
        String password = env.getProperty("spring.datasource.password");
        String dburl = env.getProperty("spring.datasource.url");

        log.info("=== Datasource configuration diagnostics ===");
        log.info("JDBC URL: {}", url);
        log.info("JDBC username: {}", username);
        log.info("JDBC driver: {}", driver);
        log.info("JDBC URL: {}", dburl);
        log.info("Password property resolved: {}", password != null);
        log.info("Password length: {}",
                password == null ? 0 : password.length());
        log.info("POSTGRES_USER environment variable present: {}",
                System.getenv("POSTGRES_USER") != null);
        log.info("POSTGRES_PASSWORD environment variable present: {}",
                System.getenv("POSTGRES_PASSWORD") != null);

        log.info("JVM timezone: "
                + TimeZone.getDefault().getID());

        log.info("JVM ZoneId: "
                + ZoneId.systemDefault());
        // Force the application to use Asia/Kolkata globally
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
    }
}