package fr.doranco.GestionStock.controller;


import fr.doranco.GestionStock.model.dto.*;
import fr.doranco.GestionStock.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping("/api/rest/users")
@RequiredArgsConstructor
public class UserController implements IUserController {

    private final IUserService userService;


    @Override
    //@PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserCreateDTO user) throws Exception {
        UserResponseDTO dto = userService.createUser(user);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }


    @Override
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) throws Exception {
        if(id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Override
    @GetMapping
    public ResponseEntity<?> getUsers() throws Exception {
        return ResponseEntity.ok(userService.getUsers());

    }
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id,@Valid @RequestBody UpdateUserDTO dto) throws Exception {
        if(id <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        return ResponseEntity.ok(userService.updateUser(id, dto));
    }

    @PatchMapping("/{id}/change-password")
    public ResponseEntity<?> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequestDTO dto) throws Exception {
        if (id <= 0) throw new IllegalArgumentException("Invalid user ID");
        userService.changePassword(id, dto);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}