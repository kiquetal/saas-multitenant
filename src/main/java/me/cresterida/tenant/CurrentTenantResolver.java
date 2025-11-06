package me.cresterida.tenant;

import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.tenant.TenantResolver;
import jakarta.enterprise.context.RequestScoped;

import java.util.logging.Logger;

@RequestScoped
@PersistenceUnitExtension
public class CurrentTenantResolver implements TenantResolver {

    private Logger LOGGER = Logger.getLogger(CurrentTenantResolver.class.getName());
    private static final String DEFAULT_TENANT = "base";

    @Override
    public String getDefaultTenantId() {
        return DEFAULT_TENANT;
    }

    @Override
    public String resolveTenantId() {

        LOGGER.info("Resolving tenant for the current request");
        // For now, always return the default tenant
        // In a real application, you would get this from a header, JWT token, or other source
        return DEFAULT_TENANT;
    }
}
