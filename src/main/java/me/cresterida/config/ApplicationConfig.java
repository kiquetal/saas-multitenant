package me.cresterida.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;


@OpenAPIDefinition(
    info = @Info(
        title = "SaaS Multi-tenant API",
        version = "1.0.0",
        description = "API for managing multi-tenant SaaS application",
        contact = @Contact(
            name = "Support",
            email = "support@example.com"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0.html"
        )
    ),
    tags = {
        @Tag(name = "Admin Operations", description = "Endpoints for tenant and organization management"),
        @Tag(name = "Tenant Operations", description = "Tenant-specific operations")
    }
)
public class ApplicationConfig extends Application {
    // This class configures:
    // 1. The base path for all REST endpoints (/api)
    // 2. The OpenAPI documentation metadata
    // It's automatically discovered by Quarkus via the @ApplicationPath annotation
}
