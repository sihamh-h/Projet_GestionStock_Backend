package fr.doranco.GestionStock.security.encoderPwd;

public interface IPasswordEncoderStrategy {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}
