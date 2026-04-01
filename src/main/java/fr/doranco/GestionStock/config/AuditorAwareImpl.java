package fr.doranco.GestionStock.config;

import fr.doranco.GestionStock.security.CustomUserDetails;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;


import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<Long> {
    @Override
    @NonNull
    public Optional<Long> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }
        Object  userPrincipal =  auth.getPrincipal();
        if(userPrincipal instanceof CustomUserDetails userDetails) {
            return Optional.of(userDetails.getId());
        }
        return Optional.empty();

    }
}
