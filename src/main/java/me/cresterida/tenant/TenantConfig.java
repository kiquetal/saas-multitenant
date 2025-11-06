package me.cresterida.tenant;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class TenantConfig {

    @ConfigProperty(name = "quarkus.hibernate-orm.database.default-schema", defaultValue = "public")
    String defaultSchema;

    public String getDefaultSchema() {
        return defaultSchema;
    }
}
