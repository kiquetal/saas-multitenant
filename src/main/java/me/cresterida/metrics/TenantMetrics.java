package me.cresterida.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TenantMetrics {

    private final MeterRegistry registry;

    @Inject
    public TenantMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordSchemaCreation(String tenantId) {
        registry.counter("tenant.schema.creation", 
            Tags.of("tenant_id", tenantId))
            .increment();
    }

    public void recordTenantOperation(String tenantId, String operation) {
        registry.counter("tenant.operations", 
            Tags.of(
                "tenant_id", tenantId,
                "operation", operation
            )).increment();
    }

    public void recordSchemaInitializationTime(String tenantId, long timeMs) {
        registry.timer("tenant.schema.initialization",
            Tags.of("tenant_id", tenantId))
            .record(() -> {
                try {
                    Thread.sleep(timeMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
    }

    public void setActiveTenantsGauge(long count) {
        registry.gauge("tenant.active.count", count);
    }
}
