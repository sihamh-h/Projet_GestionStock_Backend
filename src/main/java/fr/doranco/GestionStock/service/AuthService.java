package fr.doranco.GestionStock.service;

import fr.doranco.GestionStock.Exceptions.AdminAlreadyExistsException;
import fr.doranco.GestionStock.email.EmailService;
import fr.doranco.GestionStock.enums.TokenType;
import fr.doranco.GestionStock.enums.UserRole;
import fr.doranco.GestionStock.model.dto.LoginRequestDTO;
import fr.doranco.GestionStock.model.dto.RegisterUserRequestDTO;
import fr.doranco.GestionStock.model.dto.UserResponseDTO;
import fr.doranco.GestionStock.model.entity.Role;
import fr.doranco.GestionStock.model.entity.User;
import fr.doranco.GestionStock.repository.IRoleRepository;
import fr.doranco.GestionStock.repository.IUserRepository;
import fr.doranco.GestionStock.security.CustomUserDetails;
import fr.doranco.GestionStock.security.JwtService;
import fr.doranco.GestionStock.security.encoderPwd.PasswordEncoderContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;


import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthService implements IAuthService{

    private final IUserRepository userRepository;


    private final IRoleRepository roleRepository;


    private final PasswordEncoderContext passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    private final EmailService emailService;


    @Override
    public UserResponseDTO login(LoginRequestDTO user) throws Exception{

        if(user == null){
            throw new IllegalArgumentException("Invalid login request: user cannot be null");
        }
        if(user.getEmail() == null || user.getEmail().isBlank() || user.getPassword() == null || user.getPassword().isBlank()){
            throw new IllegalArgumentException("Invalid login request: email and password cannot be null or empty");
        }
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        user.getPassword()
                )
        );

        //boolean mustChangePassword = ((CustomUserDetails) authentication.getPrincipal()).isMustChangePassword();
        CustomUserDetails userAuth = (CustomUserDetails) authentication.getPrincipal();
        if(userAuth.isMustChangePassword()){
            throw new IllegalStateException("User must change password");
        }

        //CustomUserDetails userAuth = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userAuth, TokenType.ACCESS);
        String refreshToken = jwtService.generateToken(userAuth, TokenType.REFRESH);

        return UserResponseDTO.builder()
                .id(userAuth.getId())
                .firstname(userAuth.getFirstname())
                .lastname(userAuth.getLastname())
                .email(userAuth.getEmail())
                .roles(userAuth.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet()))
                .token(token)
                .refreshToken(refreshToken)
                .build();

    }



    @Override
    @Transactional
    public UserResponseDTO registerUser(RegisterUserRequestDTO registerUserDTO) throws Exception {
        if(registerUserDTO == null || registerUserDTO.getEmail() == null
                || registerUserDTO.getEmail().isBlank() || registerUserDTO.getUsername() == null
                || registerUserDTO.getUsername().isBlank() || registerUserDTO.getPassword() == null
                || registerUserDTO.getPassword().isBlank() || registerUserDTO.getPhoneNumber() == null || registerUserDTO.getPhoneNumber().isBlank()
                || registerUserDTO.getRoles() == null || registerUserDTO.getRoles().isEmpty() || registerUserDTO.getLastname() == null || registerUserDTO.getLastname().isBlank() || registerUserDTO.getFirstname() == null || registerUserDTO.getFirstname().isBlank()) {
            throw new IllegalArgumentException("RegisterUser cannot be null.");
        }

        if(userRepository.existsByUsername(registerUserDTO.getUsername())
                || userRepository.existsByEmail(registerUserDTO.getEmail()) || userRepository.existsByPhoneNumber(registerUserDTO.getPhoneNumber())) {
            throw new IllegalArgumentException("A user with the same username, email or phone number already exists.");
        }

        if(registerUserDTO.getRoles().contains(UserRole.ROLE_ADMIN)&&
                userRepository.existsByRoles_Name(UserRole.ROLE_ADMIN)) {
            throw new AdminAlreadyExistsException("An ADMIN already exists");
        }

        Set<Role> roleEntities = registerUserDTO.getRoles().stream()
                .map(roleEnum -> roleRepository.findByName(roleEnum)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleEnum)))
                .collect(Collectors.toSet());


        User user = User.builder()
                .firstname(registerUserDTO.getFirstname())
                .lastname(registerUserDTO.getLastname())
                .username(registerUserDTO.getUsername())
                .email(registerUserDTO.getEmail())
                .password(passwordEncoder.hash(registerUserDTO.getPassword()))
                .phoneNumber(registerUserDTO.getPhoneNumber())
                .birthdate(registerUserDTO.getBirthdate())
                .roles(roleEntities)
                .build();
        userRepository.save(user);

        String activationToken = jwtService.generateToken((new CustomUserDetails(user)), TokenType.ACTIVATION);
        emailService.sendEmail(user, activationToken, TokenType.ACTIVATION);


        return UserResponseDTO.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .roles(
                        user.getRoles()
                                .stream()
                                .map(role -> role.getName().name())
                                .collect(Collectors.toSet())
                )
                .token(activationToken)
                .build();

    }

    @Override
    public void resetPwd(String token, String newPassword) throws Exception {
        if(token == null || token.isBlank() || newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("Token and new password cannot be null or empty");
        }
        // 1. Extraire l'email du "Setup Token"
        // Le JwtService doit vérifier que le token n'est pas expiré
        Jws<Claims> jwt = jwtService.valideToken(token);
        Claims claims = jwt.getPayload();
        String type = claims.get("type", String.class);
        if(!type.equals(TokenType.PASSWORD_RESET.name()) || jwtService.isTokenExpired(token) ){
            throw new IllegalAccessException("Invalid token: incorrect type or token expired");
        }
        String email = jwtService.extractUsername(token);

        // 2. Récupérer l'utilisateur
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found : " + email));

        // 3. Sécurité supplémentaire : On vérifie qu'il DOIT encore changer son MDP
        // Cela évite qu'un ancien Setup Token soit réutilisé par erreur
        if (!user.isMustChangePassword()) {
            throw new IllegalStateException("Le mot de passe a déjà été configuré pour ce compte.");
        }

        // 4. Hachage du nouveau mot de passe
        user.setPassword(passwordEncoder.hash(newPassword));

        // 5. Mise à jour des Flags de workflow
        user.setMustChangePassword(false); // Le verrou est levé
        user.setEnabled(true);             // On confirme l'activation complète

        // 6. Sauvegarde
        userRepository.save(user);


    }

    @Override
    public UserResponseDTO refreshToken(String refreshToken) throws Exception {
        Jws<Claims> jwt = jwtService.valideToken(refreshToken);
        String type = jwt.getPayload().get("type", String.class);
        if(!type.equals(TokenType.REFRESH.name()) || jwtService.isTokenExpired(refreshToken) ){
            log.error("Type error: {}, expect: REFRESH", type);
            throw new IllegalAccessException("Invalid token: incorrect type or token expired");

        }
        String email = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found : " + email));
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String newAccessToken = jwtService.generateToken(userDetails, TokenType.ACCESS);
        String newRefreshToken = jwtService.generateToken(userDetails, TokenType.REFRESH);
        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }
}


