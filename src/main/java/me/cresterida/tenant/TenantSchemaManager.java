package me.cresterida.tenant;

import io.quarkus.runtime.StartupEvent;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import javax.sql.DataSource;

@ApplicationScoped
public class TenantSchemaManager {
    
    @Inject
    DataSource dataSource;
    
    void onStart(@Observes StartupEvent ev) {
        // Initialize default schema
        initializeSchema("public");
    }
    
    public void initializeSchema(String schemaName) {
        FluentConfiguration config = Flyway.configure()
            .dataSource(dataSource)
            .schemas(schemaName)
            .baselineOnMigrate(true)
            .baselineVersion("0.0.0")
            .locations("db/migration/" + schemaName);  // Tenant-specific migrations
            
        Flyway flyway = new Flyway(config);
        flyway.migrate();
    }
    
    public void createTenantSchema(String tenantId) {
        // Create new schema for tenant
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            
            statement.execute("CREATE SCHEMA IF NOT EXISTS " + tenantId);
            
            // Initialize schema with tenant-specific migrations
            initializeSchema(tenantId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create tenant schema: " + tenantId, e);
        }
    }
}
