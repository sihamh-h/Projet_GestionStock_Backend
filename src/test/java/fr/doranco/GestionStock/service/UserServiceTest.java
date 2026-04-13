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
import fr.doranco.GestionStock.security.JwtService;
import fr.doranco.GestionStock.security.encoderPwd.PasswordEncoderContext;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
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

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;



    @Test
    public void testCreateUserSuccessfully() throws Exception {
        UserCreateDTO user = new UserCreateDTO();
        user.setEmail("user@mail.com");
        user.setFirstname("user");
        user.setLastname("test");
        user.setPhoneNumber("0621548941");
        user.setUsername("userTest");
        user.setBirthdate(LocalDate.of( 1990, 12, 9));
        user.setRoles(Set.of(UserRole.ROLE_MANAGER));

        Role managerRole = new Role(1L, UserRole.ROLE_MANAGER,"Manager Role" , null);

        when( userRepository.existsByEmail("user@mail.com")).thenReturn(false);
        when( userRepository.existsByUsername("userTest")).thenReturn(false);
        when( userRepository.existsByPhoneNumber("0621548941")).thenReturn(false);
        when(roleRepository.findByName(UserRole.ROLE_MANAGER)).thenReturn(Optional.of(managerRole));
        when(passwordEncoder.hash(anyString())).thenReturn("hashedPassword");
        when(jwtService.generateToken(any(), eq(TokenType.ACTIVATION)))
                .thenReturn("activationToken");

        User savedUser = User.builder()
                .id(1L)
                .firstname("user")
                .lastname("test")
                .username("userTest")
                .email("user@mail.com")
                .birthdate(LocalDate.of( 1990, 12, 9))
                .roles(Set.of(managerRole))
                .password("hashedPassword")
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        UserResponseDTO response = userService.createUser(user);
        assertEquals("user", response.getFirstname());
        assertEquals("test", response.getLastname());
        assertEquals("user@mail.com", response.getEmail());
        assertEquals(Set.of("ROLE_MANAGER"), response.getRoles());
        assertEquals("activationToken", response.getToken());

        verify(userRepository).save(any(User.class));
        verify(emailService).sendEmail(any(User.class), eq("activationToken"), eq(TokenType.ACTIVATION));




    }

    @Test
    public void testCreateUserWithAdminRoleExist() {
        UserCreateDTO user = new UserCreateDTO();
        user.setEmail("user@mail.com");
        user.setFirstname("user");
        user.setLastname("test");
        user.setPhoneNumber("0621548941");
        user.setUsername("userTest");
        user.setBirthdate(LocalDate.of( 1990, 12, 9));
        user.setRoles(Set.of(UserRole.ROLE_ADMIN));

        when(userRepository.countByRoles_Name(UserRole.ROLE_ADMIN)).thenReturn(2L);
        assertThrows(AdminAlreadyExistsException.class, () -> userService.createUser(user));
    }

    @Test
    public void testCreateUserWithUsernameExist() {
        UserCreateDTO user = new UserCreateDTO();
        user.setEmail("user@mail.com");
        user.setFirstname("user");
        user.setLastname("test");
        user.setPhoneNumber("0621548941");
        user.setUsername("userTest");
        user.setBirthdate(LocalDate.of( 1990, 12, 9));
        user.setRoles(Set.of(UserRole.ROLE_MANAGER));

        when(userRepository.existsByUsername("userTest")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(user));
    }

   @Test
    public void testDeleteUserSuccessfully() throws Exception {
        Role role = new Role(1L, UserRole.ROLE_MANAGER,"Manager Role" , null);
       User user = new User();
       user.setId(1L);
       user.setRoles(Set.of(role));

       when(userRepository.findById(1L)).thenReturn(Optional.of(user));

       userService.deleteUser(1L);

       verify(userRepository).deleteById(1L);
    }

    @Test
    void testDeleteUserWhenLastAdmin() {
        Role role = new Role(1L, UserRole.ROLE_ADMIN,"Admin Role" , null);
        User lastAdmin = User.builder()
                .id(1L)
                .roles(Set.of(role))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(lastAdmin));
        when(userRepository.countByRoles_Name(UserRole.ROLE_ADMIN)).thenReturn(1L);

        assertThrows(IllegalStateException.class, () -> {
            userService.deleteUser(1L);
        });

        verify(userRepository, never()).delete(any());
    }

    @Test
    void testUpdateUserSuccessfully() throws Exception {
        Long userId = 1L;
        UpdateUserDTO dto = new UpdateUserDTO("Jean", "Dupont", "jean@test.com", "0102030405", LocalDate.of(1990,1,1), Set.of(UserRole.ROLE_MANAGER));

        User existingUser = User.builder().id(userId).roles(new HashSet<>()).build();
        Role role = new Role(1L, UserRole.ROLE_MANAGER,"Manager Role" , null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(roleRepository.findByName(UserRole.ROLE_MANAGER)).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        UserResponseDTO result = userService.updateUser(userId, dto);

        assertEquals("Jean", result.getFirstname());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void tastUpdateUserWhenUserNotFound() {
        Long userId = 1L;
        UpdateUserDTO dto = new UpdateUserDTO("Jean", "Dupont", "jean@mail.com", "0102030405", LocalDate.of(1990,1,1), Set.of(UserRole.ROLE_MANAGER));

        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> userService.updateUser(userId, dto));
    }
}
