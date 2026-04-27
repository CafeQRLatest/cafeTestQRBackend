package com.restaurant.pos.order.repository;

import com.restaurant.pos.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByClientIdOrderByCreatedAtDesc(UUID clientId);
    List<Order> findByClientIdAndOrgIdOrderByCreatedAtDesc(UUID clientId, UUID orgId);
    List<Order> findByClientIdAndOrderTypeOrderByCreatedAtDesc(UUID clientId, String orderType);
    List<Order> findByClientIdAndOrgIdAndOrderTypeOrderByCreatedAtDesc(UUID clientId, UUID orgId, String orderType);
    
    List<Order> findByClientIdAndOrderStatusInOrderByCreatedAtDesc(UUID clientId, List<String> statuses);
    List<Order> findByClientIdAndOrgIdAndOrderStatusInOrderByCreatedAtDesc(UUID clientId, UUID orgId, List<String> statuses);
    
    Optional<Order> findByIdAndClientId(UUID id, UUID clientId);
    Optional<Order> findByIdAndClientIdAndOrgId(UUID id, UUID clientId, UUID orgId);
    
    Optional<Order> findByOrderNoAndClientId(String orderNo, UUID clientId);
}
