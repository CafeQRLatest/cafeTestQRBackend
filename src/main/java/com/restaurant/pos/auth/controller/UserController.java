package com.restaurant.pos.auth.controller;

import com.restaurant.pos.auth.domain.User;
import com.restaurant.pos.auth.domain.RoleEntity;
import com.restaurant.pos.auth.repository.MenuRepository;
import com.restaurant.pos.auth.repository.UserRepository;
import com.restaurant.pos.common.dto.ApiResponse;
import com.restaurant.pos.common.tenant.TenantContext;
import com.restaurant.pos.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.restaurant.pos.auth.domain.Menu;
import com.restaurant.pos.auth.dto.UserProfileDTO;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository repository;
    private final MenuRepository menuRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<User>>> getUsers() {
        // Include users with null clientId to handle "orphans" or global users from before migration
        List<User> users = repository.findAll().stream()
                .filter(u -> u.getClientId() == null || u.getClientId().equals(TenantContext.getCurrentTenant()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody User user) {
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        if (user.getClientId() == null) {
            user.setClientId(TenantContext.getCurrentTenant());
        }
        user.setIsactive("Y");
        return ResponseEntity.ok(ApiResponse.success(repository.save(user)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable UUID id, @RequestBody User user) {
        User existingUser = repository.findById(java.util.Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Retain existing password unless an explicit new one was provided
        if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // Update other fields
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());
        existingUser.setRoleEntity(user.getRoleEntity());
        // Allow orgId to be null/empty for Global Access
        if (user.getOrgId() != null && !user.getOrgId().toString().isEmpty()) {
            existingUser.setOrgId(user.getOrgId());
        } else {
            existingUser.setOrgId(null);
        }

        existingUser.setTerminalId(user.getTerminalId());
        existingUser.setIsactive(user.getIsactive());

        return ResponseEntity.ok(ApiResponse.success(repository.save(existingUser)));
    }


    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDTO>> getMyProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        UserProfileDTO dto = UserProfileDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRoleEntity() != null ? user.getRoleEntity().getName() : "UNKNOWN")
                .build();

        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping("/menus")
    public ResponseEntity<ApiResponse<List<Menu>>> getMyMenus() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return repository.findByEmail(email)
                .map(user -> {
                    RoleEntity role = user.getRoleEntity();
                    if (role == null) return ResponseEntity.ok(ApiResponse.success(Collections.<Menu>emptyList()));

                    List<Menu> assigned = (role.getMenus() != null) 
                            ? role.getMenus().stream()
                                .filter(m -> "Y".equalsIgnoreCase(m.getIsactive()))
                                .collect(Collectors.toList())
                            : new java.util.ArrayList<>();
                    
                    boolean isSuperAdmin = "SUPER_ADMIN".equalsIgnoreCase(role.getName()) || 
                                         "ROLE_SUPER_ADMIN".equalsIgnoreCase(role.getName());

                    if (assigned.isEmpty() && isSuperAdmin) {
                        assigned = menuRepository.findAll().stream()
                                .filter(m -> "Y".equalsIgnoreCase(m.getIsactive()))
                                .filter(m -> m.getParentId() == null)
                                .collect(Collectors.toList());
                    }

                    if (assigned.isEmpty()) return ResponseEntity.ok(ApiResponse.success(assigned));

                    List<UUID> parentIds = assigned.stream().map(Menu::getId).collect(Collectors.toList());
                    List<Menu> children = menuRepository.findByParentIdIn(parentIds).stream()
                            .filter(m -> "Y".equalsIgnoreCase(m.getIsactive()))
                            .collect(Collectors.toList());

                    assigned.addAll(children);
                    List<Menu> distinctMenus = assigned.stream()
                            .collect(Collectors.toMap(Menu::getId, m -> m, (existing, replacement) -> existing))
                            .values().stream().collect(Collectors.toList());

                    return ResponseEntity.ok(ApiResponse.success(distinctMenus));
                })
                .orElse(ResponseEntity.ok(ApiResponse.success(Collections.emptyList())));
    }
}
