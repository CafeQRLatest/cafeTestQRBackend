package com.restaurant.pos.client.repository;

import com.restaurant.pos.client.domain.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {
    List<Device> findAllByClientId(UUID clientId);
    List<Device> findAllByOrgIdAndClientId(UUID orgId, UUID clientId);
    java.util.Optional<Device> findByIdAndClientId(UUID id, UUID clientId);
    java.util.Optional<Device> findByIdAndClientIdAndOrgId(UUID id, UUID clientId, UUID orgId);
}
