package me.cresterida.tenant;

import io.quarkus.runtime.StartupEvent;
import org.flywaydb.core.Flyway;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.util.logging.Logger;

@ApplicationScoped
public class TenantSchemaManager {

    @Inject
    DataSource dataSource;

    private static final String DEFAULT_TENANT = "base";
    private final Logger LOGGER = Logger.getLogger(TenantSchemaManager.class.getName());
    void onStart(@Observes StartupEvent ev) {
        // Initialize public schema for tenant metadata
        initializePublicSchema();

        Organization.findAllOrganizations()
                .stream()
                .forEach(organization -> {
                    String tenantId = organization.getTenantId();
                    LOGGER.info("Initializing schema for tenant: " + tenantId);
                    initializeTenantSchema(tenantId);
                });

    }

    private void initializePublicSchema() {
        LOGGER.info("Initializing Public Schema");
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            // Ensure public schema exists
            statement.execute("CREATE SCHEMA IF NOT EXISTS public");

            // Apply public schema migrations (metadata only)
            Flyway.configure()
                .dataSource(dataSource)
                .schemas("public")
                .locations("classpath:db/migration/public")
                .load()
                .migrate();
            LOGGER.info("Public Schema Initialized");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize public schema", e);
        }
    }

    public void initializeTenantSchema(String tenantId) {
        // Create schema if it doesn't exist
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA IF NOT EXISTS " + tenantId);

            // Apply tenant-specific migrations
            Flyway.configure()
                .dataSource(dataSource)
                .schemas(tenantId)
                .locations("classpath:db/migration/tenants")
                .load()
                .migrate();

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize tenant schema: " + tenantId, e);
        }
    }

}
