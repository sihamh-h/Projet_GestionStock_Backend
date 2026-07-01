package fr.doranco.GestionStock.security;

import org.springframework.stereotype.Component;
import org.owasp.encoder.Encode;

/**
 * Classe utilitaire pour la désinfection des entrées utilisateur.
 * Cette classe fournit une méthode pour nettoyer les chaînes de caractères en supprimant les espaces superflus
 * et en encodant les caractères spéciaux afin de prévenir les attaques XSS (Cross-Site Scripting).
 *
 * @since 2026-06-01
 * @version 1.0
 * @author Siham HA
 * @see <a href="https://owasp.org/www-project-encoder/">OWASP Encoder</a>
 */
@Component
public class InputSanitizer {
    public String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return Encode.forHtml(input.trim());
    }
}
