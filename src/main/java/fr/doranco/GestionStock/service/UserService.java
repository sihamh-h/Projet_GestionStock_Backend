package fr.doranco.GestionStock.service;

import fr.doranco.GestionStock.Exceptions.AdminAlreadyExistsException;
import fr.doranco.GestionStock.email.EmailService;
import fr.doranco.GestionStock.enums.TokenType;
import fr.doranco.GestionStock.enums.UserRole;
import fr.doranco.GestionStock.model.dto.ChangePasswordRequestDTO;
import fr.doranco.GestionStock.model.dto.UpdateUserDTO;
import fr.doranco.GestionStock.model.dto.UserCreateDTO;
import fr.doranco.GestionStock.model.dto.UserResponseDTO;
import fr.doranco.GestionStock.model.entity.Role;
import fr.doranco.GestionStock.model.entity.User;
import fr.doranco.GestionStock.mongo.service.IActivityLogService;
import fr.doranco.GestionStock.repository.IPasswordHistoryRepository;
import fr.doranco.GestionStock.repository.IRoleRepository;
import fr.doranco.GestionStock.repository.IUserRepository;
import fr.doranco.GestionStock.security.CustomUserDetails;
import fr.doranco.GestionStock.security.InputSanitizer;
import fr.doranco.GestionStock.security.JwtService;
import fr.doranco.GestionStock.security.encoderPwd.PasswordEncoderContext;
import fr.doranco.GestionStock.security.pwd.PasswordPolicyValidator;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import fr.doranco.GestionStock.model.entity.PasswordHistory;
import fr.doranco.GestionStock.mongo.dto.ActivityLogRequestDTO;
import org.passay.RuleResult;
import java.time.LocalDateTime;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Service de gestion des utilisateurs.
 * Ce service fournit des méthodes pour gérer les utilisateurs, y compris la création, la mise à jour, la suppression et la récupération des informations des utilisateurs.
 * Il gère également les changements de mot de passe et l'application de la politique de mot de passe.
 *
 * @since 2026-06-01
 * @author Siham HA
 * @version 1.0
 * @see IUserService
 * @see IUserRepository
 * @see IRoleRepository
 * @see PasswordEncoderContext
 * @see EmailService
 * @see JwtService
 * @see IPasswordHistoryRepository
 * @see PasswordPolicyValidator
 * @see IActivityLogService
 * @see InputSanitizer
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserService {

    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final PasswordEncoderContext passwordEncoder;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final IPasswordHistoryRepository passwordHistoryRepository;
    private final PasswordPolicyValidator passwordPolicyValidator;
    private final IActivityLogService activityLogService;
    private final InputSanitizer sanitizer;

    @Value("${app.stock.max-admins:2}")
    private int maxAdmins;

    /**
     * Récupère la liste de tous les utilisateurs.
     * @return une liste de UserResponseDTO représentant tous les utilisateurs.
     * @throws Exception si une erreur survient lors de la récupération des utilisateurs.
     * @author Siham HA
     * @since 2026-06-01
     * @see IUserRepository
     * @see UserResponseDTO
     */
    @Override
    public List<UserResponseDTO> getUsers() throws Exception {
            List<User> users = userRepository.findAll();
            return users.stream()
                    .map(user -> UserResponseDTO.builder()
                            .id(user.getId())
                            .firstname(user.getFirstname())
                            .lastname(user.getLastname())
                            .email(user.getEmail())
                            .phoneNumber(user.getPhoneNumber())
                            .birthdate(user.getBirthdate())
                            .roles(
                                    user.getRoles()
                                            .stream()
                                            .map(role -> role.getName().name())
                                            .collect(Collectors.toSet())
                            )
                            .build())
                    .collect(Collectors.toList());

    }

    /**
     * Récupère un utilisateur par son ID.
     * @param id l'ID de l'utilisateur à récupérer.
     * @return un UserResponseDTO représentant l'utilisateur correspondant à l'ID fourni.
     * @throws Exception si l'utilisateur n'est pas trouvé ou si l'ID est invalide.
     * @author Siham HA
     * @since 2026-06-01
     * @see IUserRepository
     * @see UserResponseDTO
     */
    @Override
    public UserResponseDTO getUserById(Long id) throws Exception {
        if(id <= 0) {
            throw new IllegalArgumentException("User ID must be greater than zero.");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));
        return mapToResponseDTO(user);
    }

    /**
     * Met à jour les informations d'un utilisateur existant.
     * @param id l'ID de l'utilisateur à mettre à jour.
     * @param updateUserDto un objet UpdateUserDTO contenant les nouvelles informations de l'utilisateur.
     * @return un UserResponseDTO représentant l'utilisateur mis à jour.
     * @throws Exception si l'utilisateur n'est pas trouvé, si les données sont invalides, ou si une erreur survient lors de la mise à jour.
     * @author Siham HA
     * @since 2026-06-01
     * @see IUserRepository
     * @see IRoleRepository
     * @see UserResponseDTO
     * @see UpdateUserDTO
     */
    @Override
    public UserResponseDTO  updateUser(Long id, UpdateUserDTO updateUserDto) throws Exception {
        if(id == null || id <= 0) {
            throw new IllegalArgumentException("User ID must be greater than zero or can not be null.");
        }
        if(updateUserDto == null || updateUserDto.getFirstname() == null || updateUserDto.getFirstname().isBlank()
                || updateUserDto.getLastname() == null || updateUserDto.getLastname().isBlank()
                || updateUserDto.getEmail() == null || updateUserDto.getEmail().isBlank()
                ||updateUserDto.getPhoneNumber() == null || updateUserDto.getPhoneNumber().isBlank()
                || updateUserDto.getBirthdate() == null || updateUserDto.getRoles() == null
                || updateUserDto.getRoles().isEmpty()) {
            throw new IllegalArgumentException("UpdateUserDTO and its fields cannot be null or empty.");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));

        if (userRepository.existsByEmailAndIdNot(updateUserDto.getEmail(), id)) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (userRepository.existsByPhoneNumberAndIdNot(updateUserDto.getPhoneNumber(), id)) {
            throw new IllegalArgumentException("Phone number already exists");
        }
        user.setFirstname(sanitizer.sanitize(updateUserDto.getFirstname()));
        user.setLastname(sanitizer.sanitize(updateUserDto.getLastname()));
        user.setEmail(updateUserDto.getEmail());
        user.setPhoneNumber(updateUserDto.getPhoneNumber());
        user.setBirthdate(updateUserDto.getBirthdate());

        boolean wasAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals(UserRole.ROLE_ADMIN));

        boolean willBeAdmin = updateUserDto.getRoles().contains(UserRole.ROLE_ADMIN);

        if (wasAdmin && !willBeAdmin) {
            long adminCount = userRepository.countByRoles_Name(UserRole.ROLE_ADMIN);
            if (adminCount <= 1) {
                throw new IllegalStateException("Cannot remove ADMIN role from the last remaining admin.");
            }
        }

        if (!wasAdmin && willBeAdmin) {
            long adminCount = userRepository.countByRoles_Name(UserRole.ROLE_ADMIN);
            if (adminCount >= maxAdmins) {
                throw new IllegalStateException("Maximum number of ADMIN users has been reached.");
            }
        }
        Set<Role> roles = updateUserDto.getRoles().stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid role: " + roleName)))
                .collect(Collectors.toSet());

        user.setRoles(roles);
        if (updateUserDto.getImageUrl() != null) {
            user.setImageUrl(updateUserDto.getImageUrl());
        }
        userRepository.save(user);
        return mapToResponseDTO(user);

    }


    /**
     * Supprime un utilisateur en le marquant comme supprimé et désactivé.
     * @param id l'ID de l'utilisateur à supprimer.
     * @throws Exception si l'utilisateur n'est pas trouvé, si l'ID est invalide, ou si la suppression de l'utilisateur violerait les contraintes de rôle (par exemple, supprimer le dernier utilisateur ADMIN).
     * @author Siham HA
     * @since 2026-06-01
     * @see IUserRepository
     * @see User
     */
    @Override
    @Transactional
    public void deleteUser(Long id) throws Exception {
        if(id == null || id <= 0) {
            throw new IllegalArgumentException("User ID must be greater than zero or can not be null.");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals(UserRole.ROLE_ADMIN));

        if (isAdmin) {
            long adminCount = userRepository.countByRoles_Name(UserRole.ROLE_ADMIN);
            if (adminCount <= 1) {
                throw new IllegalStateException("Cannot delete the last remaining ADMIN user.");
            }
        }
        user.setDeleted(true);
        user.setEnabled(false);
        userRepository.save(user);
        log.info("User with ID {} has been deleted.", id);

    }

    /**
     * Crée un nouvel utilisateur ou restaure un utilisateur supprimé.
     * @param registerUserDTO un objet UserCreateDTO contenant les informations de l'utilisateur à créer.
     * @return un UserResponseDTO représentant l'utilisateur créé ou restauré.
     * @throws Exception si les données d'inscription sont invalides, si l'email, le nom d'utilisateur ou le numéro de téléphone existe déjà, ou si la création de l'utilisateur viole les contraintes de rôle (par exemple, dépasser le nombre maximum d'utilisateurs ADMIN).
     * author Siham HA
     * @since 2026-06-01
     * @see IUserRepository
     * @see IRoleRepository
     * @see UserResponseDTO
     * @see UserCreateDTO
     * @see User
     */
    @Override
    @Transactional
    public UserResponseDTO createUser(UserCreateDTO registerUserDTO) throws Exception {

        if(registerUserDTO == null || registerUserDTO.getEmail() == null
                || registerUserDTO.getEmail().isBlank() || registerUserDTO.getUsername() == null
                || registerUserDTO.getUsername().isBlank() || registerUserDTO.getPhoneNumber() == null || registerUserDTO.getPhoneNumber().isBlank()
                || registerUserDTO.getRoles() == null || registerUserDTO.getRoles().isEmpty() || registerUserDTO.getLastname() == null || registerUserDTO.getLastname().isBlank() || registerUserDTO.getFirstname() == null || registerUserDTO.getFirstname().isBlank()) {
            throw new IllegalArgumentException("RegisterUser cannot be null.");
        }

        Optional<User> emailUser = userRepository.findByEmailIncludingDeleted(registerUserDTO.getEmail());
        Optional<User> usernameUser = userRepository.findByUsernameIncludingDeleted(registerUserDTO.getUsername());
        Optional<User> phoneUser = userRepository.findByPhoneNumberIncludingDeleted(registerUserDTO.getPhoneNumber());

        if (emailUser.isPresent()) {
            User u = emailUser.get();
            if (!u.isDeleted()) {
                throw new IllegalArgumentException("email already exists.");
            }
            return restoreDeletedUser(u, registerUserDTO);
        }


        if (usernameUser.isPresent()) {
            User u = usernameUser.get();
            if (!u.isDeleted()) {
                throw new IllegalArgumentException("Username already exists.");
            }
            return restoreDeletedUser(u, registerUserDTO);
        }


        if (phoneUser.isPresent()) {
            User u = phoneUser.get();
            if (!u.isDeleted()) {
                throw new IllegalArgumentException("Phone number already exists.");
            }
            return restoreDeletedUser(u, registerUserDTO);
        }


        if (registerUserDTO.getRoles().contains(UserRole.ROLE_ADMIN)) {
            checkAdminLimit();
        }

        User user = User.builder()
                .firstname(sanitizer.sanitize(registerUserDTO.getFirstname()))
                .lastname(sanitizer.sanitize(registerUserDTO.getLastname()))
                .username(sanitizer.sanitize(registerUserDTO.getUsername()))
                .email(registerUserDTO.getEmail())
                .phoneNumber(registerUserDTO.getPhoneNumber())
                .birthdate(registerUserDTO.getBirthdate())
                .roles(mapRoles(registerUserDTO.getRoles()))
                .build();

        return finalizeUserRegistration(user);
    }

    /**
     * Change le mot de passe d'un utilisateur.
     * @param userId l'ID de l'utilisateur dont le mot de passe doit être changé.
     * @param dto un objet ChangePasswordRequestDTO contenant le mot de passe actuel et le nouveau mot de passe.
     * @throws Exception si l'utilisateur n'est pas trouvé, si le mot de passe actuel est incorrect, si le nouveau mot de passe ne respecte pas la politique de sécurité, ou si le nouveau mot de passe a déjà été utilisé récemment.
     * @author Siham HA
     * @since 2026-06-01
     * @see IUserRepository
     * @see IPasswordHistoryRepository
     * @see PasswordPolicyValidator
     * @see PasswordEncoderContext
     * @see User
     */
    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequestDTO dto) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        if (!passwordEncoder.matches(dto.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }

        if (dto.currentPassword().equals(dto.newPassword())) {
            throw new IllegalArgumentException("New password must be different from current password.");
        }

        RuleResult policyResult = passwordPolicyValidator.validate(dto.newPassword());
        if (!policyResult.isValid()) {
            throw new IllegalArgumentException(passwordPolicyValidator.getMessages(policyResult));
        }

        List<PasswordHistory> history = passwordHistoryRepository.findTop3ByUserOrderByCreatedAtDesc(user);

        for (PasswordHistory entry : history) {
            if (passwordEncoder.matches(dto.newPassword(), entry.getHashedPassword())) {
                throw new IllegalArgumentException("New password has already been used recently.");
            }
        }

        passwordHistoryRepository.save(PasswordHistory.builder()
                .user(user)
                .hashedPassword(user.getPassword())
                .createdAt(LocalDateTime.now())
                .build());

        user.setPassword(passwordEncoder.hash(dto.newPassword()));
        userRepository.save(user);

        activityLogService.log(ActivityLogRequestDTO.builder()
                .userId(user.getId())
                .userEmail(user.getEmail())
                .action("PASSWORD_CHANGE")
                .description("Password changed successfully")
                .success(true)
                .build());

        log.info("[USER] Password changed for user ID: {}", userId);
    }


    /**
     * Restaure un utilisateur supprimé et met à jour ses informations.
     * @param user l'utilisateur supprimé à restaurer.
     * @param dto un objet UserCreateDTO contenant les nouvelles informations de l'utilisateur.
     * @return un UserResponseDTO représentant l'utilisateur restauré.
     * @throws Exception si une erreur survient lors de la restauration de l'utilisateur.
     * @author Siham HA
     * @since 2026-06-01
     * @see IUserRepository
     * @see IRoleRepository
     * @see UserResponseDTO
     * @see UserCreateDTO
     * @see User
     */
    private UserResponseDTO restoreDeletedUser(User user, UserCreateDTO dto) throws Exception {

        log.warn("[USER] Restoring deleted user ID: {}", user.getId());

        user.setDeleted(false);
        updateUserFields(user, dto);

        if (dto.getRoles().contains(UserRole.ROLE_ADMIN)) {
            checkAdminLimit();
        }

        return finalizeUserRegistration(user);
    }

    /**
     * Finalise l'inscription d'un utilisateur en générant un mot de passe temporaire, en le hachant, en sauvegardant l'utilisateur et en envoyant un email d'activation.
     * @param user l'utilisateur à finaliser l'inscription.
     * @return un UserResponseDTO représentant l'utilisateur finalisé avec le token d'activation.
     * @author Siham HA
     * @since 2026-06-01
     * @see IUserRepository
     * @see EmailService
     * @see JwtService
     * @see UserResponseDTO
     * @see User
     */
    private UserResponseDTO finalizeUserRegistration(User user) {
        String temporaryPassword = generateSecurePassword();
        user.setPassword(passwordEncoder.hash(temporaryPassword));

        userRepository.save(user);

        String activationToken = jwtService.generateToken(new CustomUserDetails(user), TokenType.ACTIVATION);
        emailService.sendEmail(user, activationToken, TokenType.ACTIVATION);

        log.info("[USER] Registration finalized for: {}", user.getEmail());

        UserResponseDTO response = mapToResponseDTO(user);
        response.setToken(activationToken);
        return response;
    }

    /**
     * Vérifie si le nombre maximum d'utilisateurs ADMIN a été atteint.
     * @throws AdminAlreadyExistsException si le nombre maximum d'utilisateurs ADMIN a été atteint.
     * @author Siham HA
     * @since 2026-06-01
     * @see IUserRepository
     * @see UserRole
     */
    private void checkAdminLimit() throws AdminAlreadyExistsException {
        long currentAdminCount = userRepository.countByRoles_Name(UserRole.ROLE_ADMIN);
        if (currentAdminCount >= maxAdmins) {
            throw new AdminAlreadyExistsException("Maximum number of ADMIN users reached.");
        }
    }

    /**
     * Mappe un ensemble d'énumérations de rôles d'utilisateur à un ensemble d'entités Role.
     * @param roleEnums un ensemble d'énumérations de rôles d'utilisateur à mapper.
     * @return un ensemble d'entités Role correspondant aux énumérations de rôles fournies.
     * @throws RuntimeException si un rôle n'est pas trouvé dans le repository.
     * @author Siham HA
     * @since 2026-06-01
     * @see IRoleRepository
     * @see Role
     * @see UserRole
     */
    private Set<Role> mapRoles(Set<UserRole> roleEnums) {
        return roleEnums.stream()
                .map(roleEnum -> roleRepository.findByName(roleEnum)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleEnum)))
                .collect(Collectors.toSet());
    }

    /**
     * Met à jour les champs d'un utilisateur avec les informations fournies dans un UserCreateDTO.
     * @param user l'utilisateur à mettre à jour.
     * @param dto un objet UserCreateDTO contenant les nouvelles informations de l'utilisateur.
     * @author Siham HA
     * @since 2026-06-01
     * @see User
     * @see UserCreateDTO
     */
    private void updateUserFields(User user, UserCreateDTO dto) {
        user.setFirstname(sanitizer.sanitize(dto.getFirstname()));
        user.setLastname(sanitizer.sanitize(dto.getLastname()));
        user.setRoles(mapRoles(dto.getRoles()));
    }

    private void validateCreateDTO(UserCreateDTO dto) {
        if (dto == null || isBlank(dto.getEmail()) || isBlank(dto.getUsername())
                || isBlank(dto.getFirstname()) || dto.getRoles() == null || dto.getRoles().isEmpty()) {
            throw new IllegalArgumentException("Required user fields are missing.");
        }
    }

    /**
     * Vérifie si une chaîne de caractères est vide ou nulle.
     * @param s la chaîne de caractères à vérifier.
     * @return true si la chaîne est vide ou nulle, false sinon.
     * @author Siham HA
     * @since 2026-06-01
     */
    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    /**
     * Génère un mot de passe sécurisé aléatoire respectant les critères de complexité.
     * @return un mot de passe sécurisé aléatoire.
     * @author Siham HA
     * @since 2026-06-01
     * @see SecureRandom
     */
    private String generateSecurePassword() {
        String upperCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String specialChars = "!@#$%^&*()-_=+";
        String combinedChars = upperCaseLetters + lowerCaseLetters + numbers + specialChars;

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        password.append(upperCaseLetters.charAt(random.nextInt(upperCaseLetters.length())));
        password.append(lowerCaseLetters.charAt(random.nextInt(lowerCaseLetters.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));
        password.append(specialChars.charAt(random.nextInt(specialChars.length())));

        for(int i = 4; i <= 12; i++) {
            password.append(combinedChars.charAt(random.nextInt(combinedChars.length())));
        }

        return password.toString();
    }

    /**
     * Mappe un objet User en un objet UserResponseDTO.
     * @param user l'utilisateur à mapper.
     * @return un UserResponseDTO représentant l'utilisateur fourni.
     * @author Siham HA
     * @since 2026-06-01
     * @see User
     * @see UserResponseDTO
     */
    private UserResponseDTO mapToResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .birthdate(user.getBirthdate())
                .imageUrl(user.getImageUrl())
                .roles(
                        user.getRoles()
                                .stream()
                                .map(role -> role.getName().name())
                                .collect(Collectors.toSet())
                )
                .build();
    }


}

