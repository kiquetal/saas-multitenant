package me.cresterida.tenant;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.List;

@Entity
@Table(name = "organizations", schema = "public")
public class Organization extends PanacheEntity {
    @Column(name = "name")
    public String name;

    @Column(name = "tenant_id")
    public String tenantId;

    @Column(name = "subscription_plan")
    public String subscriptionPlan;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getSubscriptionPlan() {
        return subscriptionPlan;
    }

    public void setSubscriptionPlan(String subscriptionPlan) {
        this.subscriptionPlan = subscriptionPlan;
    }


    public static Organization findByTenantId(String tenantId) {
        return find("tenantId", tenantId).firstResult();
    }

    public static List<Organization> findAllOrganizations() {
        return listAll();
    }

}
