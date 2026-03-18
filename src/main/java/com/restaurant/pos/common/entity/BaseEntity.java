package com.restaurant.pos.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity extends AuditableEntity {

    @Column(name = "client_id", updatable = false)
    private UUID clientId;

    @Column(name = "org_id", updatable = false)
    private UUID orgId;

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        // Auto-set client ID if it's available in the context and not already set
        if (this.clientId == null) {
            UUID currentClient = com.restaurant.pos.common.tenant.TenantContext.getCurrentTenant();
            if (currentClient != null) {
                this.clientId = currentClient;
            }
        }
        
        // Auto-set org ID if it's available in the context and not already set
        if (this.orgId == null) {
            UUID currentOrg = com.restaurant.pos.common.tenant.TenantContext.getCurrentOrg();
            if (currentOrg != null) {
                this.orgId = currentOrg;
            }
        }
    }
}
