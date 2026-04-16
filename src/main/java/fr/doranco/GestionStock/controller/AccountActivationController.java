package fr.doranco.GestionStock.controller;


import fr.doranco.GestionStock.service.AccountActivateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/rest/auth")
@RequiredArgsConstructor
@Slf4j
public class AccountActivationController {
    private final AccountActivateService accountActivationService;

    @GetMapping("/activate")
    public ResponseEntity<?> activateAccount(@RequestParam String token) {
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "token is required"));
        }
        String setupToken;
        try {
            setupToken = accountActivationService.activateAccount(token);
        } catch (IllegalAccessException e) {
            log.error("Account activation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("invalid token or user not found", e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("Account activation failed: {}", e.getMessage());
            return ResponseEntity.accepted().body(Map.of("account already activated", e.getMessage()));
        }
        return ResponseEntity.ok(Map.of("message", "account activated successfully", "setupToken", setupToken));
    }
}
