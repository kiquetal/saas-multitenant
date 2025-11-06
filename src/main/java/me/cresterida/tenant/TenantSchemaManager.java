package me.cresterida.tenant;

import io.quarkus.runtime.StartupEvent;
import org.flywaydb.core.Flyway;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.util.logging.Logger;

@ApplicationScoped
public class TenantSchemaManager {

    @Inject
    DataSource dataSource;

    private static final String DEFAULT_TENANT = "base";
    private final Logger LOGGER = Logger.getLogger(TenantSchemaManager.class.getName());

    void onStart(@Observes StartupEvent ev) {
        try {
            // Initialize public schema for tenant metadata
            initializePublicSchema();

            // Verify table exists before proceeding

            // Set current tenant to public before querying organizations

            // Now it's safe to query organizations
            Organization.findAllOrganizations()
                    .stream()
                    .forEach(organization -> {
                        String tenantId = organization.getTenantId();
                        LOGGER.info("Initializing schema for tenant: " + tenantId);
                        initializeTenantSchema(tenantId);
                    });
        } catch (Exception e) {
            LOGGER.severe("Error during startup: " + e.getMessage());
            throw e;
        }
    }

    private void verifyOrganizationsTable() {
        LOGGER.info("Verifying Organizations Table");
        try (var connection = dataSource.getConnection()) {
            // Set search path to public schema
            try (var stmt = connection.createStatement()) {
                stmt.execute("SET search_path TO public");
            }

            // Check if table exists
            try (ResultSet rs = connection.getMetaData().getTables(null, "public", "organizations", new String[]{"TABLE"})) {
                if (!rs.next()) {
                    LOGGER.severe("Organizations table not found!");
                    throw new RuntimeException("Organizations table does not exist!");
                }
            }

            // Verify we can query the table
            try (var stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM public.organizations");
                if (rs.next()) {
                    int count = rs.getInt(1);
                    LOGGER.info("Found " + count + " organizations in the database");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify organizations table", e);
        }
    }

    private void initializePublicSchema() {
        LOGGER.info("Initializing Public Schema");
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            // Ensure public schema exists and set it as default
            statement.execute("CREATE SCHEMA IF NOT EXISTS public");
            statement.execute("SET search_path TO public");

            // Apply public schema migrations (metadata only)
            Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas("public")
                .defaultSchema("public")
                .locations("classpath:db/migration/public")
                .load();

            flyway.migrate();

            // Give the database a moment to complete the operation
            Thread.sleep(1000);

            LOGGER.info("Public Schema Initialized");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize public schema", e);
        }
    }

    public void initializeTenantSchema(String tenantId) {
        // Create schema if it doesn't exist
        LOGGER.info("Creating schema for tenant: " + tenantId);
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA IF NOT EXISTS " + quoteTenantIdentifier(tenantId));

            // Apply tenant-specific migrations
            Flyway.configure()
                .dataSource(dataSource)
                .schemas(tenantId)
                .locations("classpath:db/migration/tenants")
                .load()
                .migrate();
            LOGGER.info("Schema Created");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize tenant schema: " + tenantId, e);
        }
    }

    private String quoteTenantIdentifier(String tenantId) {
        return "\"" + tenantId.replace("\"", "\"\"") + "\"";
    }
}
