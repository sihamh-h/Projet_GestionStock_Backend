package fr.doranco.GestionStock.security.encoderPwd;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;

@Component("argon2Strategy")
public class Argon2PasswordEncoderStrategy implements IPasswordEncoderStrategy {
    private final Argon2PasswordEncoder encoder;
    public Argon2PasswordEncoderStrategy() {
        this.encoder = new Argon2PasswordEncoder(
                16,
                32,
                1,
                65536,
                3
        );
    }
    @Override
    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }
    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
