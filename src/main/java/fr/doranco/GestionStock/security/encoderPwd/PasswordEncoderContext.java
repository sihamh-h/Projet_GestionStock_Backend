package fr.doranco.GestionStock.security.encoderPwd;

import org.springframework.stereotype.Component;

@Component
public class PasswordEncoderContext {
    private final IPasswordEncoderStrategy strategy;

    // @Primary injecte automatiquement la bonne stratégie
    public PasswordEncoderContext(IPasswordEncoderStrategy strategy) {
        this.strategy = strategy;
    }

    public String hash(String rawPassword) {
        return strategy.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String hashedPassword) {
        return strategy.matches(rawPassword, hashedPassword);
    }
}
