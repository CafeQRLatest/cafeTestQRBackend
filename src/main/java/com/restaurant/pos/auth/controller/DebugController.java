package com.restaurant.pos.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/debug")
@RequiredArgsConstructor
public class DebugController {
    private final com.restaurant.pos.client.repository.ClientRepository clientRepository;
    private final com.restaurant.pos.auth.repository.UserRepository userRepository;

    @GetMapping("/check-client")
    public ResponseEntity<?> checkClient(@RequestParam String email) {
        var user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.ok("User not found: " + email);
        
        var clientByEmail = clientRepository.findByEmail(email).orElse(null);
        var clientById = clientRepository.findById(java.util.Objects.requireNonNull(user.getClientId() != null ? user.getClientId() : UUID.randomUUID())).orElse(null);
        
        return ResponseEntity.ok(java.util.Map.of(
            "user_email", user.getEmail(),
            "user_clientId", user.getClientId() != null ? user.getClientId().toString() : "NULL",
            "clientByEmail", clientByEmail != null ? clientByEmail.getSubscriptionStatus() : "NOT_FOUND",
            "clientById", clientById != null ? clientById.getSubscriptionStatus() : "NOT_FOUND"
        ));
    }
}
