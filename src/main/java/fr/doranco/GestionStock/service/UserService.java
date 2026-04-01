package fr.doranco.GestionStock.service;

import fr.doranco.GestionStock.Exceptions.AdminAlreadyExistsException;
import fr.doranco.GestionStock.enums.UserRole;
import fr.doranco.GestionStock.model.dto.RegisterUserRequestDTO;
import fr.doranco.GestionStock.model.dto.UserResponseDTO;
import fr.doranco.GestionStock.model.entity.Role;
import fr.doranco.GestionStock.model.entity.User;
import fr.doranco.GestionStock.repository.IRoleRepository;
import fr.doranco.GestionStock.repository.IUserRepository;
import fr.doranco.GestionStock.security.encoderPwd.PasswordEncoderContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService implements IUserService {


    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private  IRoleRepository roleRepository;

    @Autowired
    private PasswordEncoderContext passwordEncoder;

    @Override
    public List<User> getUsers() throws Exception {
        return userRepository.findAll();
    }

    @Override
    public User getUser(long id) throws Exception {
        if(id <= 0) {
            throw new IllegalArgumentException("User ID must be greater than zero.");
        }
        Optional<User> user = userRepository.findById(id);
        return user.orElse(null);
    }

    @Override
    public User getUser(String email) throws Exception {
        if(email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty.");
        }
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public void addOrUpdateUser(User user) throws Exception {
        if(user == null || user.getUsername() == null || user.getUsername().isBlank() || user.getEmail() == null
                || user.getEmail().isBlank() ) {
            throw new IllegalArgumentException("User cannot be null.");
        }

    }

    @Override
    public void deleteUser(long id) throws Exception {

    }

    @Override
    public List<User> getUsersByRoleId(long roleId) throws Exception {
        return List.of();
    }

    @Override
    public String getRoleByUserId(long userId) throws Exception {
        return "";
    }

    @Override
    public List<String> getPermissionsByUserId(long userId) throws Exception {
        return List.of();
    }

    @Override
    public int countUsersByRoleId(long roleId) throws Exception {
        return 0;
    }

    @Override
    public void createAdmin(User admin) throws Exception {


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

