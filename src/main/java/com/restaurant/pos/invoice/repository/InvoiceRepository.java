package com.restaurant.pos.invoice.repository;

import com.restaurant.pos.invoice.domain.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    Optional<Invoice> findByOrderIdAndClientId(UUID orderId, UUID clientId);
    Optional<Invoice> findByOrderIdAndClientIdAndOrgId(UUID orderId, UUID clientId, UUID orgId);
    
    Optional<Invoice> findByIdAndClientId(UUID id, UUID clientId);
    Optional<Invoice> findByIdAndClientIdAndOrgId(UUID id, UUID clientId, UUID orgId);
    
    Optional<Invoice> findByInvoiceNumberAndClientId(String invoiceNumber, UUID clientId);
    Optional<Invoice> findByInvoiceNumberAndClientIdAndOrgId(String invoiceNumber, UUID clientId, UUID orgId);
}
