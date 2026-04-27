package com.restaurant.pos.purchasing.repository;

import com.restaurant.pos.purchasing.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    List<Customer> findByClientIdOrderByNameAsc(UUID clientId);
    List<Customer> findByClientIdAndOrgIdOrderByNameAsc(UUID clientId, UUID orgId);
    Optional<Customer> findByIdAndClientId(UUID id, UUID clientId);
    Optional<Customer> findByIdAndClientIdAndOrgId(UUID id, UUID clientId, UUID orgId);
    Optional<Customer> findByPhoneAndClientIdAndOrgId(String phone, UUID clientId, UUID orgId);
    Optional<Customer> findByPhoneAndClientId(String phone, UUID clientId);
    Optional<Customer> findByEmailAndClientId(String email, UUID clientId);
}
