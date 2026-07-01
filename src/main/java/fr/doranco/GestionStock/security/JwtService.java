package fr.doranco.GestionStock.security;

import fr.doranco.GestionStock.enums.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Service for handling JWT (JSON Web Token) operations such as token generation, validation, and extraction of claims.
 * This service uses the jjwt library for JWT operations and relies on JwtProperties for configuration.
 * @since 2026-06-01
 * @version 1.0
 * @author Siham HA
 * @see JwtProperties
 * @see TokenType
 */
@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtProperties properties;

    /**
     * Generates a JWT token for the given user details and token type.
     * The token includes claims for the user's username, ID, roles, and the type of token. The token is signed using a secret key and has an expiration time based on the token type.
     * @param userDetails the user details for which the token is generated
     * @param typeToken the type of token to generate (e.g., ACCESS, REFRESH)
     * @return the generated JWT token as a String
     * @throws IllegalArgumentException if the userDetails is not an instance of CustomUserDetails
     * @see CustomUserDetails
     * @see TokenType
     */
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

    /**
     * Retrieves the signing key used for signing JWT tokens. The key is derived from the secret specified in JwtProperties and is decoded from Base64 format.
     * @return the SecretKey used for signing JWT tokens
     * @see JwtProperties
     * @see SecretKey
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(properties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Validates the given JWT token and returns the claims contained within it. The token is parsed and verified using the secret key specified in JwtProperties.
     * @param token the JWT token to validate
     * @return the object containing the claims from the validated token
     * @throws io.jsonwebtoken.JwtException if the token is invalid or cannot be parsed
     * @see JwtProperties
     * @see Jws
     * @see Claims
     */
    public Jws<Claims> valideToken(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.getSecret())))
                .build()
                .parseSignedClaims(token);
    }

    /**
     * Checks if the given JWT token has expired. The expiration date is extracted from the token's claims and compared to the current date.
     * @param token the JWT token to check for expiration
     * @return true if the token has expired, false otherwise
     * @throws io.jsonwebtoken.JwtException if the token is invalid or cannot be parsed
     * @see Jws
     * @see Claims
     * @see Date
     */
    public boolean isTokenExpired(String token) {
        return valideToken(token).getPayload().getExpiration().before(new Date());
    }

    /**
     * Extracts the username (subject) from the given JWT token. The username is retrieved from the token's claims after validating the token.
     * @param token the JWT token from which to extract the username
     * @return the username (subject) contained in the token
     * @throws io.jsonwebtoken.JwtException if the token is invalid or cannot be parsed
     * @see Jws
     * @see Claims
     * @see String
     */
    public String extractUsername(String token) {
        return valideToken(token).getPayload().getSubject();
    }

    /**
     * Validates the given JWT token against the provided user details. The token is considered valid if the username extracted from the token matches the username of the user and if the token has not expired.
     * @param token the JWT token to validate
     * @param user the user details against which to validate the token
     * @return true if the token is valid for the given user, false otherwise
     * @throws io.jsonwebtoken.JwtException if the token is invalid or cannot be parsed
     * @see UserDetails
     * @see String
     * @see boolean
     */
    public boolean isTokenValid(String token, UserDetails user) {
        return extractUsername(token).equals(user.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Extracts the expiration date from the given JWT token. The expiration date is retrieved from the token's claims after validating the token.
     * @param token the JWT token from which to extract the expiration date
     * @return the expiration date contained in the token
     * @throws io.jsonwebtoken.JwtException if the token is invalid or cannot be parsed
     * @see Jws
     * @see Claims
     * @see Date
     */
    public Date extractExpiration(String token) {
        return valideToken(token).getPayload().getExpiration();
    }
}
