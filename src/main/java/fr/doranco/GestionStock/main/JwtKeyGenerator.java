package fr.doranco.GestionStock.main;

import javax.crypto.SecretKey;
import java.util.Base64;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.MacAlgorithm;

public class JwtKeyGenerator {
    public static void main(String[] args) {

        MacAlgorithm algo = Jwts.SIG.HS512;

        SecretKey key = algo.key().build();

        String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());

        System.out.println("JWT Activation Secret (Base64):");
        System.out.println(base64Key);
        System.out.println(System.getenv("JWT_ACTIVATION_SECRET"));
    }

}
