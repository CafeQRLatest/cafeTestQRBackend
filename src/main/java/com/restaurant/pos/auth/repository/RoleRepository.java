package com.restaurant.pos.auth.repository;

import com.restaurant.pos.auth.domain.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {
    Optional<RoleEntity> findByName(String name);
    Optional<RoleEntity> findByNameAndClientId(String name, UUID clientId);
    Optional<RoleEntity> findByNameAndClientIdIsNull(String name);
}
