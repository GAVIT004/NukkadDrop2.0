package nukkaddrop_backend.controller;

import nukkaddrop_backend.dto.LoginRequest;
import nukkaddrop_backend.dto.LoginResponse;
import nukkaddrop_backend.entity.User;
import nukkaddrop_backend.service.AuthService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request) {

        User user = authService.authenticate(
                request.getEmail(),
                request.getPassword()
        );

        // Invalid credentials
        if (user == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password");
        }

        // Generate JWT
        String token = authService.generateToken(user);

        // Return token + user
        return ResponseEntity.ok(
                new LoginResponse(token, user)
        );
    }
}