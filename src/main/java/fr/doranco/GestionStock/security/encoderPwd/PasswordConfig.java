package fr.doranco.GestionStock.security.encoderPwd;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {

    @Value("${app.security.password-encoder:bcrypt}")
    private String encoderType;

    @Bean
    @Primary
    public IPasswordEncoderStrategy passwordEncoderStrategy(
            @Qualifier("bcryptStrategy") IPasswordEncoderStrategy bcrypt,
            @Qualifier("argon2Strategy") IPasswordEncoderStrategy argon2) {

        return switch (encoderType) {
            case "bcrypt" -> bcrypt;
            case "argon2" -> argon2;
            default -> throw new IllegalArgumentException(
                    "Unknown encoder type: " + encoderType
            );
        };
    }
    @Bean
    public PasswordEncoder passwordEncoder(PasswordEncoderContext context) {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return context.hash(rawPassword.toString());
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return context.matches(rawPassword.toString(), encodedPassword);
            }
        };
    }
}
