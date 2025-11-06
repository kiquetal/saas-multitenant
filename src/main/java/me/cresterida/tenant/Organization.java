package me.cresterida.tenant;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "organizations")
public class Organization extends PanacheEntity {
    
    public String name;
    public String tenantId;
    public String subscriptionPlan;
    
    public static Organization findByTenantId(String tenantId) {
        return find("tenantId", tenantId).firstResult();
    }
}
