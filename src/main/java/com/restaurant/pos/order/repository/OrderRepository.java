package com.restaurant.pos.order.repository;

import com.restaurant.pos.order.domain.Order;
import com.restaurant.pos.order.domain.OrderType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {
    List<Order> findByClientIdOrderByCreatedAtDesc(UUID clientId);
    List<Order> findByClientIdAndOrgIdOrderByCreatedAtDesc(UUID clientId, UUID orgId);
    // Use OrderType enum — Spring Data JPA handles @Enumerated(STRING) automatically
    List<Order> findByClientIdAndOrderTypeOrderByCreatedAtDesc(UUID clientId, OrderType orderType);
    List<Order> findByClientIdAndOrgIdAndOrderTypeOrderByCreatedAtDesc(UUID clientId, UUID orgId, OrderType orderType);

    List<Order> findByClientIdAndOrderStatusInOrderByCreatedAtDesc(UUID clientId, List<String> statuses);
    List<Order> findByClientIdAndOrgIdAndOrderStatusInOrderByCreatedAtDesc(UUID clientId, UUID orgId, List<String> statuses);

    Optional<Order> findByIdAndClientId(UUID id, UUID clientId);
    Optional<Order> findByIdAndClientIdAndOrgId(UUID id, UUID clientId, UUID orgId);

    Optional<Order> findByOrderNoAndClientId(String orderNo, UUID clientId);

    long countByClientId(UUID clientId);
}
