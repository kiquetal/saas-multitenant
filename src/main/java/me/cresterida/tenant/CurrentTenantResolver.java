package me.cresterida.tenant;

import io.quarkus.hibernate.orm.runtime.tenant.TenantResolver;
import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class CurrentTenantResolver implements TenantResolver {
    
    private static final String DEFAULT_TENANT = "public";
    
    @Override
    public String getDefaultTenantId() {
        return DEFAULT_TENANT;
    }

    @Override
    public String resolveTenantId() {
        // For now, always return the default tenant
        // In a real application, you would get this from a header, JWT token, or other source
        return DEFAULT_TENANT;
    }
}
