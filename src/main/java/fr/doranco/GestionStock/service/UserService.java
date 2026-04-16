package fr.doranco.GestionStock.service;

import fr.doranco.GestionStock.Exceptions.AdminAlreadyExistsException;
import fr.doranco.GestionStock.email.EmailService;
import fr.doranco.GestionStock.enums.TokenType;
import fr.doranco.GestionStock.enums.UserRole;
import fr.doranco.GestionStock.model.dto.UpdateUserDTO;
import fr.doranco.GestionStock.model.dto.UserCreateDTO;
import fr.doranco.GestionStock.model.dto.UserResponseDTO;
import fr.doranco.GestionStock.model.entity.Role;
import fr.doranco.GestionStock.model.entity.User;
import fr.doranco.GestionStock.repository.IRoleRepository;
import fr.doranco.GestionStock.repository.IUserRepository;
import fr.doranco.GestionStock.security.CustomUserDetails;
import fr.doranco.GestionStock.security.JwtService;
import fr.doranco.GestionStock.security.encoderPwd.PasswordEncoderContext;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Value("${app.stock.max-admins:2}")
    private int maxAdmins;


    @Override
    public List<UserResponseDTO> getUsers() throws Exception {
            List<User> users = userRepository.findAll();
            return users.stream()
                    .map(user -> UserResponseDTO.builder()
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
                            .build())
                    .collect(Collectors.toList());

    }

    @Override
    public UserResponseDTO getUserById(Long id) throws Exception {
        if(id <= 0) {
            throw new IllegalArgumentException("User ID must be greater than zero.");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + id));
        return mapToResponseDTO(user);
    }


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
        user.setFirstname(updateUserDto.getFirstname());
        user.setLastname(updateUserDto.getLastname());
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
        userRepository.save(user);
        return mapToResponseDTO(user);



    }

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





    @Override
    @Transactional
    public UserResponseDTO createUser(UserCreateDTO registerUserDTO) throws Exception {

        if(registerUserDTO == null || registerUserDTO.getEmail() == null
                || registerUserDTO.getEmail().isBlank() || registerUserDTO.getUsername() == null
                || registerUserDTO.getUsername().isBlank() || registerUserDTO.getPhoneNumber() == null || registerUserDTO.getPhoneNumber().isBlank()
                || registerUserDTO.getRoles() == null || registerUserDTO.getRoles().isEmpty() || registerUserDTO.getLastname() == null || registerUserDTO.getLastname().isBlank() || registerUserDTO.getFirstname() == null || registerUserDTO.getFirstname().isBlank()) {
            throw new IllegalArgumentException("RegisterUser cannot be null.");
        }

        if(userRepository.countByUsernameIncludingDeleted(registerUserDTO.getUsername()) >0
                || userRepository.countByEmailIncludingDeleted(registerUserDTO.getEmail())>0 || userRepository.countByPhoneNumberIncludingDeleted(registerUserDTO.getPhoneNumber())>0) {
            throw new IllegalArgumentException("A user with the same username, email or phone number already exists.");
        }

        long currentAdminCount = userRepository.countByRoles_Name(UserRole.ROLE_ADMIN);

        if (registerUserDTO.getRoles().contains(UserRole.ROLE_ADMIN) && currentAdminCount >= maxAdmins) {
            throw new AdminAlreadyExistsException("Maximum number of ADMIN users (" + maxAdmins + ") has already been reached.");
        }

        Set<Role> roleEntities = registerUserDTO.getRoles().stream()
                .map(roleEnum -> roleRepository.findByName(roleEnum)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleEnum)))
                .collect(Collectors.toSet());

        String temporaryPassword = generateSecurePassword();

        User user = User.builder()
                .firstname(registerUserDTO.getFirstname())
                .lastname(registerUserDTO.getLastname())
                .username(registerUserDTO.getUsername())
                .email(registerUserDTO.getEmail())
                .password(passwordEncoder.hash(temporaryPassword))
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

    private UserResponseDTO mapToResponseDTO(User user) {
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
                .build();
    }



}

