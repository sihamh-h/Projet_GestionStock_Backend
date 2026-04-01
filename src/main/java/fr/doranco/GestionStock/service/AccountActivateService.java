package fr.doranco.GestionStock.service;

import fr.doranco.GestionStock.enums.TokenType;
import fr.doranco.GestionStock.model.entity.User;
import fr.doranco.GestionStock.repository.IUserRepository;
import fr.doranco.GestionStock.security.CustomUserDetails;
import fr.doranco.GestionStock.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountActivateService {

    private final JwtService jwtService;
    private final IUserRepository userRepository;

    public String activateAccount(String token) throws IllegalAccessException, IllegalStateException {

        if(token == null || token.isBlank()){
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        Jws<Claims> jwt = jwtService.valideToken(token);
        Claims claims = jwt.getPayload();
        String type = claims.get("type", String.class);
        if(!type.equals(TokenType.ACTIVATION.name()) || jwtService.isTokenExpired(token)){
            throw new IllegalAccessException("Invalid token: incorrect type or token expired");
        }
        Long userId = claims.get("uid", Long.class);
        if(userId == null){
            throw new IllegalAccessException("Invalid token: userId is invalid");
        }

        User user = userRepository.findById(userId).orElseThrow(
                () -> new IllegalAccessException("User not found")
        );
        if(user.isEnabled()){
            throw new IllegalStateException("Account already active");
        }
        user.setEnabled(true);
        userRepository.save(user);
        return jwtService.generateToken(new CustomUserDetails(user), TokenType.PASSWORD_RESET);
    }
}
