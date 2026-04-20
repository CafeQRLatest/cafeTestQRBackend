package com.restaurant.pos.common.repository;

import com.restaurant.pos.common.entity.SystemConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SystemConfigurationRepository extends JpaRepository<SystemConfiguration, UUID> {
    // Since there's only one row, we can just find any or the first one if multiple exist (though seed ensures only one).
}
