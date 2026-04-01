package fr.doranco.GestionStock.security.encoderPwd;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component("bcryptStrategy")
public class BCryptPasswordEncoderStrategy implements IPasswordEncoderStrategy {

    private final BCryptPasswordEncoder encoder;

    public BCryptPasswordEncoderStrategy() {
        this.encoder = new BCryptPasswordEncoder();
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
