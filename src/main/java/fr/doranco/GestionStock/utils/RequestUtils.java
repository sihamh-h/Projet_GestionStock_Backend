package fr.doranco.GestionStock.utils;

import fr.doranco.GestionStock.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class RequestUtils {

    private RequestUtils() {}

    public static Long getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) return null;
            return ((CustomUserDetails) auth.getPrincipal()).getId();
        } catch (Exception e) {
            return null;
        }
    }

    public static String getCurrentUserEmail() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) return "unknown";
            return ((CustomUserDetails) auth.getPrincipal()).getUsername();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
