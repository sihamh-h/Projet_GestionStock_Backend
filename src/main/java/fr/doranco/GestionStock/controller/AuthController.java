package fr.doranco.GestionStock.controller;

import fr.doranco.GestionStock.model.dto.*;
import fr.doranco.GestionStock.service.IAuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping("/api/rest/auth")
public class AuthController implements IAuthController{

    @Autowired
    private  IAuthService authService;

    // http://localhost:8080/api/rest/auth/login
    @PostMapping("/login")
    @Override
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO user) throws BadCredentialsException, DisabledException, Exception {
        if(user == null){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        UserResponseDTO dto = authService.login(user);
        //return ResponseEntity.ok(dto);
        return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "User", dto
        ));
    }

    @Override
    //@PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterUserRequestDTO user) throws Exception {
        //if(user ==null)
        UserResponseDTO dto = authService.registerUser(user);
        return new ResponseEntity<>(dto,HttpStatus.CREATED);
        //return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    // http://localhost:8080/api/rest/auth/reset-password
    @PostMapping("/reset-password")
    @Override
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPwdRequestDTO request) throws Exception {
        authService.resetPwd(request.token(), request.password());
        return ResponseEntity.ok(Map.of("message", "password reset successful"));
    }

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) throws Exception {
        authService.refreshToken(request.refreshToken());
        return ResponseEntity.ok(Map.of("message", "Token refreshed successfully"));

    }
}
