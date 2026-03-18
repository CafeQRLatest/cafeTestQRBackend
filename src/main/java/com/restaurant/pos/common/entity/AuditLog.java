package com.restaurant.pos.common.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "audit_logs")
public class AuditLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID orgId;
    private UUID terminalId;
    private UUID userId;

    private String action; // CREATE, UPDATE, DELETE, LOGIN, etc.
    private String entityName;
    private String entityId;

    private String ipAddress;
}
