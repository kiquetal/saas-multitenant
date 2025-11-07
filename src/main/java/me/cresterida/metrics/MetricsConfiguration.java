package me.cresterida.metrics;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.util.Arrays;

@Singleton
public class MetricsConfiguration {
    
    @ConfigProperty(name = "quarkus.application.name", defaultValue = "saas-multitenant")
    String applicationName;

    @ConfigProperty(name = "quarkus.profile", defaultValue = "dev")
    String environment;

    @ApplicationScoped
    public MeterFilter configureAllRegistries() {
        return MeterFilter.commonTags(Arrays.asList(
            Tag.of("application", applicationName),
            Tag.of("env", environment),
            Tag.of("version", "1.0.0")
        ));
    }
}
