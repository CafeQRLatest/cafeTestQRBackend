package com.restaurant.pos.client.domain;

import com.restaurant.pos.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "devices")
public class Device extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "device_type", nullable = false)
    private String deviceType;

    @Column(name = "serial_number")
    private String serialNumber;

    @Builder.Default
    @Column(name = "isactive", length = 1)
    @com.fasterxml.jackson.annotation.JsonProperty("isactive")
    private String isactive = "Y";

    public boolean isActive() {
        return "Y".equalsIgnoreCase(isactive);
    }
}
