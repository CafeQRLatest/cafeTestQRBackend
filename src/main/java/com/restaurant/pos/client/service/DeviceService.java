package com.restaurant.pos.client.service;

import com.restaurant.pos.client.domain.Device;
import com.restaurant.pos.client.repository.DeviceRepository;
import com.restaurant.pos.common.exception.ResourceNotFoundException;
import com.restaurant.pos.common.tenant.TenantContext;
import com.restaurant.pos.common.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceService {

    private final DeviceRepository repository;

    public List<Device> getMyDevices() {
        UUID tenantId = TenantContext.getCurrentTenant();
        UUID orgId = TenantContext.getCurrentOrg();
        
        if (SecurityUtils.isSuperAdmin() || SecurityUtils.hasRole("ADMIN")) {
            return repository.findAllByClientId(tenantId);
        }
        
        return repository.findAllByOrgIdAndClientId(orgId, tenantId);
    }

    public Device getDeviceById(UUID id) {
        UUID tenantId = TenantContext.getCurrentTenant();
        java.util.Optional<Device> device;
        
        if (SecurityUtils.isSuperAdmin() || SecurityUtils.hasRole("ADMIN")) {
            device = repository.findByIdAndClientId(id, tenantId);
        } else {
            device = repository.findByIdAndClientIdAndOrgId(id, tenantId, TenantContext.getCurrentOrg());
        }
        
        return device.orElseThrow(() -> new ResourceNotFoundException("Device not found or access denied"));
    }

    @Transactional
    public Device saveDevice(Device device) {
        log.info("Saving device: {}", device);
        device.setClientId(TenantContext.getCurrentTenant());
        
        if (!SecurityUtils.isSuperAdmin() || device.getOrgId() == null) {
            device.setOrgId(TenantContext.getCurrentOrg());
        }
        
        if (device.getIsactive() == null) {
            device.setIsactive("Y");
        }
        
        return repository.save(device);
    }

    @Transactional
    public void deleteDevice(UUID id) {
        Device device = getDeviceById(id);
        device.setIsactive("N");
        repository.save(device);
    }
}
