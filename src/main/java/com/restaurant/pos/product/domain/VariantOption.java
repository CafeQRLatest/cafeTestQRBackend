package com.restaurant.pos.product.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.restaurant.pos.common.entity.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "variant_options")
public class VariantOption extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Builder.Default
    private UUID id = null;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private VariantGroup group;

    private String name;
    
    @Builder.Default
    private BigDecimal additionalPrice = BigDecimal.ZERO;

    @Builder.Default
    @JsonProperty("isActive")
    private boolean isActive = true;

    @Column(name = "client_id")
    private UUID clientId;

    @Column(name = "org_id")
    private UUID orgId;

    @JsonProperty("groupId")
    public UUID getGroupId() {
        return group != null ? group.getId() : null;
    }

    @JsonProperty("groupId")
    public void setGroupId(UUID groupId) {
        if (groupId == null) {
            return;
        }

        if (this.group == null) {
            this.group = new VariantGroup();
        }
        this.group.setId(groupId);
    }
}
