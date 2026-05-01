package com.restaurant.pos.sequence.domain;

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
@Table(name = "document_sequences", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"client_id", "org_id", "document_type"})
})
public class DocumentSequence extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Builder.Default
    private UUID id = null;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", length = 50, nullable = false)
    private DocumentType documentType;

    @Column(length = 20)
    private String prefix;

    @Column(length = 20)
    private String suffix;

    @Column(name = "padding_length", nullable = false)
    @Builder.Default
    private Integer paddingLength = 5;

    @Column(name = "next_number", nullable = false)
    @Builder.Default
    private Long nextNumber = 1L;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    // Optimistic locking as a secondary defense, though we will use pessimistic lock for generation
    @Version
    private Long version;
}
