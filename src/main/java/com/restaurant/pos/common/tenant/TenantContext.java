package com.restaurant.pos.common.tenant;

import java.util.UUID;

/**
 * Legacy bridge to UserContext.
 * All multi-tenancy logic should gradually migrate to UserContext.
 */
public class TenantContext {

    public static UUID getCurrentTenant() {
        return UserContext.getCurrentTenant();
    }

    public static void setCurrentTenant(UUID tenant) {
        UserContext.setCurrentTenant(tenant);
    }
    
    public static UUID getCurrentOrg() {
        return UserContext.getCurrentOrg();
    }

    public static void setCurrentOrg(UUID org) {
        UserContext.setCurrentOrg(org);
    }
    
    public static UUID getCurrentTerminal() {
        return UserContext.getCurrentTerminal();
    }

    public static void setCurrentTerminal(UUID terminal) {
        UserContext.setCurrentTerminal(terminal);
    }

    public static void clear() {
        UserContext.clear();
    }
}
