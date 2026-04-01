package fr.doranco.GestionStock.controller;


import fr.doranco.GestionStock.model.dto.LoginRequestDTO;
import fr.doranco.GestionStock.model.dto.RegisterUserRequestDTO;
import fr.doranco.GestionStock.model.dto.UserResponseDTO;
import fr.doranco.GestionStock.service.IUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping("/api/rest/users")
public class UserController implements IUserController {

    @Autowired
    private IUserService userService;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    //@PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@Valid @RequestBody RegisterUserRequestDTO user) throws Exception {
        //if(user ==null)
        UserResponseDTO dto = userService.registerUser(user);
        return new ResponseEntity<>(dto,HttpStatus.CREATED);
        //return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }




// http://localhost:8080/api/rest/users/login
   /* @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequestDTO user) throws BadCredentialsException , DisabledException, Exception  {
        if(user == null){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        user.getPassword()
                )
        );
        return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "email", authentication.getName()
        ));
    }*/
}
