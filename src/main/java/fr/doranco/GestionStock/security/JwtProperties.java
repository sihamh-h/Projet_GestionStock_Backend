package fr.doranco.GestionStock.security;

import fr.doranco.GestionStock.enums.TokenType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


@ConfigurationProperties(prefix = "app.jwt")
@Data
public class JwtProperties {

    private String secret;
    private Map<String, Long> tokens = new HashMap<>();

    public long getExpiration(TokenType type) {
        Long expiration = tokens.get(type.name());
        if (expiration == null || expiration <= 0) {
            throw new IllegalArgumentException("Expiration time not defined for token type: " + type);
        }
        return expiration;
    }


}
