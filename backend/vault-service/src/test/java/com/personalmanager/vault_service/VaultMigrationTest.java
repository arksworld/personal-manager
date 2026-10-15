package com.personalmanager.vault_service;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class VaultMigrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("pm_vault_test")
                    .withUsername("pm_test")
                    .withPassword("pm_test");

    @Test
    void migrationCreatesFeatureModuleTable() throws Exception {
        Flyway flyway = Flyway.configure()
                .dataSource(
                        postgres.getJdbcUrl(),
                        postgres.getUsername(),
                        postgres.getPassword())
                .load();

        var result = flyway.migrate();

        assertEquals(1, result.migrationsExecuted);

        try (var connection = DriverManager.getConnection(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword());
             var statement = connection.prepareStatement("""
                     SELECT COUNT(*)
                     FROM information_schema.tables
                     WHERE table_schema = 'public'
                       AND table_name = 'vault_item'
                     """);
             var rows = statement.executeQuery()) {

            assertTrue(rows.next());
            assertEquals(1, rows.getInt(1));
        }
    }
}