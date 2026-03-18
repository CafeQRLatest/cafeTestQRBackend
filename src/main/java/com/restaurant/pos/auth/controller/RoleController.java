package com.restaurant.pos.auth.controller;

import com.restaurant.pos.auth.domain.RoleEntity;
import com.restaurant.pos.auth.domain.Permission;
import com.restaurant.pos.auth.domain.Menu;
import com.restaurant.pos.auth.service.RoleService;
import com.restaurant.pos.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleEntity>>> getRoles() {
        return ResponseEntity.ok(ApiResponse.success(service.getMyRoles()));
    }

    @GetMapping("/permissions")
    public ResponseEntity<ApiResponse<List<Permission>>> getPermissions() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllPermissions()));
    }

    @GetMapping("/menus")
    public ResponseEntity<ApiResponse<List<Menu>>> getMenus() {
        return ResponseEntity.ok(ApiResponse.success(service.getAllMenus()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RoleEntity>> createRole(@RequestBody RoleEntity role) {
        return ResponseEntity.ok(ApiResponse.success(service.saveRole(role)));
    }

}
