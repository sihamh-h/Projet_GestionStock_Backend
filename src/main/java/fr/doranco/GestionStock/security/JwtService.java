package fr.doranco.GestionStock.security;

import fr.doranco.GestionStock.enums.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtProperties properties;

    public String generateToken(UserDetails userDetails, TokenType typeToken) {
        long expiration = properties.getExpiration(typeToken);
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("uid", ((CustomUserDetails) userDetails).getId())
                .claim("roles", userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .claim("type", typeToken.name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(properties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Jws<Claims> valideToken(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.getSecret())))
                .build()
                .parseSignedClaims(token);
    }
    public boolean isTokenExpired(String token) {
        return valideToken(token).getPayload().getExpiration().before(new Date());
    }

    public String extractUsername(String token) {
        return valideToken(token).getPayload().getSubject();
    }

    public boolean isTokenValid(String token, UserDetails user) {
        return extractUsername(token).equals(user.getUsername()) && !isTokenExpired(token);
    }
}
