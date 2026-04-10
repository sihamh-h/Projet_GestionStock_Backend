package fr.doranco.GestionStock.service;

import fr.doranco.GestionStock.repository.IRoleRepository;
import fr.doranco.GestionStock.repository.IUserRepository;
import fr.doranco.GestionStock.security.JwtService;
import fr.doranco.GestionStock.security.encoderPwd.PasswordEncoderContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private IUserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private IRoleRepository roleRepository;

    @Mock
    private PasswordEncoderContext passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    public void testRefreshToken_InvalidToken() {
        String invalidToken = "invalid.token.here";
        when(jwtService.valideToken(invalidToken)).thenThrow(new SignatureException("Invalid token"));
        assertThrows(SignatureException.class, () -> {
            authService.refreshToken(invalidToken);
        });

    }

    @Test
    public void testTokenNull() {
       String token = null;
       when(jwtService.valideToken(token)).thenThrow(new IllegalArgumentException("Token cannot be null"));
       assertThrows(IllegalArgumentException.class, () -> { authService.refreshToken(token);
       });
    }

    @Test
    public void testTokenTypeInvalid() {
        String token = "valid.Access.token";
        Claims claims = Jwts.claims().add("type", "Access").build();
        Jws<Claims> mockJws = mock(Jws.class);
        when(mockJws.getPayload()).thenReturn(claims);
        when(jwtService.valideToken(token)).thenReturn(mockJws);
        assertThrows(IllegalAccessException.class, () -> {
            authService.refreshToken(token);
        });
    }


}
