package fr.doranco.GestionStock.service;

import fr.doranco.GestionStock.Exceptions.AdminAlreadyExistsException;
import fr.doranco.GestionStock.email.EmailService;
import fr.doranco.GestionStock.enums.TokenType;
import fr.doranco.GestionStock.enums.UserRole;
import fr.doranco.GestionStock.model.dto.LoginRequestDTO;
import fr.doranco.GestionStock.model.dto.RegisterUserRequestDTO;
import fr.doranco.GestionStock.model.dto.UserResponseDTO;
import fr.doranco.GestionStock.model.entity.PasswordHistory;
import fr.doranco.GestionStock.model.entity.Role;
import fr.doranco.GestionStock.model.entity.User;
import fr.doranco.GestionStock.mongo.document.InvalidatedToken;
import fr.doranco.GestionStock.mongo.dto.ActivityLogRequestDTO;
import fr.doranco.GestionStock.mongo.repository.IInvalidatedTokenRepository;
import fr.doranco.GestionStock.mongo.service.IActivityLogService;
import fr.doranco.GestionStock.repository.IPasswordHistoryRepository;
import fr.doranco.GestionStock.repository.IRoleRepository;
import fr.doranco.GestionStock.repository.IUserRepository;
import fr.doranco.GestionStock.security.CustomUserDetails;
import fr.doranco.GestionStock.security.InputSanitizer;
import fr.doranco.GestionStock.security.JwtService;
import fr.doranco.GestionStock.security.encoderPwd.PasswordEncoderContext;
import fr.doranco.GestionStock.security.pwd.PasswordPolicyValidator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Service d'authentification et de gestion des utilisateurs.
 * Ce service gère les opérations liées à l'authentification des utilisateurs,
 * y compris la connexion, l'inscription, la réinitialisation du mot de passe et le rafraîchissement des tokens.
 *
 * @since 2026-06-01
 * @author Siham HA
 * @version 1.0
 * @see IUserRepository
 * @see IRoleRepository
 * @see PasswordEncoderContext
 * @see AuthenticationManager
 * @see JwtService
 * @see EmailService
 * @see IActivityLogService
 * @see IInvalidatedTokenRepository
 * @see IPasswordHistoryRepository
 * @see PasswordPolicyValidator
 * @see InputSanitizer
 */
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
    private final IActivityLogService activityLogService;
    private final IInvalidatedTokenRepository invalidatedTokenRepository;
    private final IPasswordHistoryRepository passwordHistoryRepository;
    private final PasswordPolicyValidator passwordPolicyValidator;
    private final InputSanitizer sanitizer;



    /**
     * Authentifie un utilisateur en fonction de ses informations de connexion.
     * Cette méthode vérifie les informations d'identification de l'utilisateur, gère les tentatives de connexion échouées, et génère un token JWT pour l'utilisateur authentifié.
     * @param user les informations de connexion de l'utilisateur (email et mot de passe)
     * @return un objet UserResponseDTO contenant les informations de l'utilisateur et le token JWT
     * @throws Exception si les informations de connexion sont invalides, si le compte est verrouillé, ou si l'utilisateur doit changer son mot de passe
     * @since 2026-06-01
     * @see UserResponseDTO
     * @see LoginRequestDTO
     * @see JwtService
     * @see IActivityLogService
     * @see IInvalidatedTokenRepository
     * @see IPasswordHistoryRepository
     * @see PasswordPolicyValidator
     * @see InputSanitizer
     */
    @Override
    @Transactional(dontRollbackOn = {BadCredentialsException.class, IllegalStateException.class})
    public UserResponseDTO login(LoginRequestDTO user) throws Exception{
        if(user == null){
            throw new IllegalArgumentException("Invalid login request: user cannot be null");
        }
        if(user.getEmail() == null || user.getEmail().isBlank() || user.getPassword() == null || user.getPassword().isBlank()){
            throw new IllegalArgumentException("Invalid login request: email and password cannot be null or empty");
        }
        User userEntity = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (userEntity.getLockTime() != null && userEntity.getLockTime().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Account locked. Try again in 15 minutes.");
        }
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getEmail(),
                            user.getPassword()
                    )
            );
        } catch(Exception e){
            int attempts = userEntity.getFailedLoginAttempts() + 1;
            userEntity.setFailedLoginAttempts(attempts);
            if (attempts >= 5) {
                userEntity.setLockTime(LocalDateTime.now().plusMinutes(15));
                userEntity.setFailedLoginAttempts(0);
                userRepository.save(userEntity);
                activityLogService.log(ActivityLogRequestDTO.builder()
                        .userEmail(user.getEmail())
                        .action("ACCOUNT_LOCKED")
                        .description("Account locked after 5 failed attempts")
                        .success(false)
                        .build());
                throw new IllegalStateException("Account locked after 5 failed attempts. Try again in 15 minutes.");
            }
            userRepository.save(userEntity);
            throw new BadCredentialsException("Invalid email or password");

        }

        userEntity.setFailedLoginAttempts(0);
        userEntity.setLockTime(null);
        userRepository.save(userEntity);


        CustomUserDetails userAuth = (CustomUserDetails) authentication.getPrincipal();
        if(userAuth.isMustChangePassword()){
            activityLogService.log(ActivityLogRequestDTO.builder()
                    .userId(userAuth.getId())
                    .userEmail(user.getEmail())
                    .action("LOGIN")
                    .description("Failed login attempt due to mustChangePassword")
                    .success(false)
                    .build());
            throw new IllegalStateException("User must change password");
        }

        activityLogService.log(ActivityLogRequestDTO.builder()
                .userId(userAuth.getId())
                .userEmail(user.getEmail())
                .action("LOGIN")
                .description("Successful login")
                .success(true)
                .build());
        String token = jwtService.generateToken(userAuth, TokenType.ACCESS);
        String refreshToken = jwtService.generateToken(userAuth, TokenType.REFRESH);


        return UserResponseDTO.builder()
                .id(userAuth.getId())
                .firstname(userAuth.getFirstname())
                .lastname(userAuth.getLastname())
                .email(userAuth.getEmail())
                .imageUrl(userAuth.getImageUrl())
                .roles(userAuth.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet()))
                .token(token)
                .refreshToken(refreshToken)
                .build();

    }

    /**
     * Enregistre un nouvel utilisateur dans le système.
     * Cette méthode vérifie les informations de l'utilisateur, s'assure qu'aucun utilisateur avec le même nom d'utilisateur, email ou numéro de téléphone n'existe déjà, et crée un nouvel utilisateur avec les rôles spécifiés. Si l'utilisateur est enregistré avec succès, un email d'activation est envoyé.
     * @param registerUserDTO les informations de l'utilisateur à enregistrer
     * @return un objet UserResponseDTO contenant les informations de l'utilisateur enregistré et le token d'activation
     * @throws Exception si les informations de l'utilisateur sont invalides ou si un utilisateur avec le même nom d'utilisateur, email ou numéro de téléphone existe déjà
     * @since 2026-06-01
     * @see UserResponseDTO
     * @see RegisterUserRequestDTO
     * @see JwtService
     * @see EmailService
     * @see IActivityLogService
     * @see IRoleRepository
     * @see IUserRepository
     * @see InputSanitizer
     * @see PasswordEncoderContext
     * @see UserRole
     */
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
                .firstname(sanitizer.sanitize(registerUserDTO.getFirstname()))
                .lastname(sanitizer.sanitize(registerUserDTO.getLastname()))
                .username(sanitizer.sanitize(registerUserDTO.getUsername()))
                .email(registerUserDTO.getEmail())
                .password(passwordEncoder.hash(registerUserDTO.getPassword()))
                .phoneNumber(registerUserDTO.getPhoneNumber())
                .birthdate(registerUserDTO.getBirthdate())
                .roles(roleEntities)
                .mustChangePassword(false)
                .build();
        userRepository.save(user);

        activityLogService.log(ActivityLogRequestDTO.builder()
                .userId(user.getId())
                .userEmail(user.getEmail())
                .action("USER_CREATE")
                .description("User registered successfully : " + user.getEmail())
                .success(true)
                .build());

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

    /**
     * Réinitialise le mot de passe d'un utilisateur en utilisant un token de réinitialisation.
     * Cette méthode valide le token de réinitialisation, vérifie que le nouveau mot de passe n'a pas été utilisé récemment, met à jour le mot de passe de l'utilisateur et enregistre l'ancien mot de passe dans l'historique. Elle invalide également le token utilisé pour éviter toute réutilisation.
     * @param token le token de réinitialisation du mot de passe
     * @param newPassword le nouveau mot de passe à définir pour l'utilisateur
     * @throws Exception si le token est invalide, expiré, déjà utilisé ou si le nouveau mot de passe a été utilisé récemment
     * @since 2026-06-01
     * @see JwtService
     * @see IInvalidatedTokenRepository
     * @see IPasswordHistoryRepository
     * @see IUserRepository
     * @see IActivityLogService
     * @see PasswordEncoderContext
     * @see PasswordHistory
     * @see User
     * @see TokenType
     */
    @Override
    public void resetPwd(String token, String newPassword) throws Exception {
        if(token == null || token.isBlank() || newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("Token and new password cannot be null or empty");
        }
        Jws<Claims> jwt = jwtService.valideToken(token);
        Claims claims = jwt.getPayload();
        String type = claims.get("type", String.class);
        if(!type.equals(TokenType.PASSWORD_RESET.name()) || jwtService.isTokenExpired(token) ){
            throw new IllegalAccessException("Invalid token: incorrect type or token expired");
        }
        if (invalidatedTokenRepository.existsByToken(token)) {
            throw new IllegalStateException("Token already used");
        }
        String email = jwtService.extractUsername(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found : " + email));

        List<PasswordHistory> history = passwordHistoryRepository.findTop3ByUserOrderByCreatedAtDesc(user);
        for (PasswordHistory entry : history) {
            if (passwordEncoder.matches(newPassword, entry.getHashedPassword())) {
                throw new IllegalArgumentException("New password has already been used recently.");
            }
        }

        passwordHistoryRepository.save(PasswordHistory.builder()
                .user(user)
                .hashedPassword(user.getPassword())
                .createdAt(LocalDateTime.now())
                .build());

        user.setPassword(passwordEncoder.hash(newPassword));
        if (user.isMustChangePassword()) {
            user.setMustChangePassword(false);
            user.setEnabled(true);
        }
        userRepository.save(user);
        invalidatedTokenRepository.save(InvalidatedToken.builder()
                .token(token)
                .reason("PASSWORD_RESET_USED")
                .expiresAt(jwtService.extractExpiration(token))
                .build());

        activityLogService.log(ActivityLogRequestDTO.builder()
                .userEmail(email)
                .action("PASSWORD_RESET")
                .description("Mot de passe réinitialisé pour : " + email)
                .success(true)
                .build());


    }

    /**
     * Initie le processus de réinitialisation du mot de passe pour un utilisateur en fonction de son email.
     * Cette méthode vérifie si l'utilisateur existe et est activé, génère un token de réinitialisation du mot de passe, et envoie un email à l'utilisateur avec le lien de réinitialisation. Si l'utilisateur n'existe pas ou n'est pas activé, un message d'information est enregistré dans les logs.
     * @param email l'email de l'utilisateur pour lequel la réinitialisation du mot de passe est demandée
     * @throws Exception si une erreur survient lors de la génération du token ou de l'envoi de l'email
     * @since 2026-06-01
     * @see JwtService
     * @see EmailService
     * @see IUserRepository
     * @see IActivityLogService
     * @see TokenType
     */
    @Override
    @Transactional
    public void initiatePasswordReset(String email) throws Exception {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (user.isEnabled()) {
                String resetToken = jwtService.generateToken(
                        new CustomUserDetails(user),
                        TokenType.PASSWORD_RESET
                );

                emailService.sendEmail(user, resetToken, TokenType.PASSWORD_RESET);

                log.info("[AUTH] Demande de reset MDP envoyée pour : {}", email);
            } else {
                log.warn("[AUTH] Tentative de reset MDP pour un compte inactif : {}", email);
            }
        } else {
            log.info("[AUTH] Tentative de reset MDP pour un email inconnu : {}", email);
        }
    }

    /**
     * Rafraîchit le token d'accès d'un utilisateur en utilisant un token de rafraîchissement valide.
     * Cette méthode valide le token de rafraîchissement, vérifie son type et sa validité, puis génère un nouveau token d'accès et un nouveau token de rafraîchissement pour l'utilisateur. Si le token de rafraîchissement est invalide ou expiré, une exception est levée.
     * @param refreshToken le token de rafraîchissement utilisé pour obtenir un nouveau token d'accès
     * @return un objet UserResponseDTO contenant les informations de l'utilisateur et les nouveaux tokens d'accès et de rafraîchissement
     * @throws Exception si le token de rafraîchissement est invalide, expiré ou de type incorrect
     * @since 2026-06-01
     * @see JwtService
     * @see IUserRepository
     * @see UserResponseDTO
     * @see CustomUserDetails
     * @see TokenType
     */
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


