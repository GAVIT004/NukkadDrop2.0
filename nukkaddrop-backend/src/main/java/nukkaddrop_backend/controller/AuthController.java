package nukkaddrop_backend.controller;

import nukkaddrop_backend.dto.LoginRequest;
import nukkaddrop_backend.dto.LoginResponse;
import nukkaddrop_backend.entity.User;
import nukkaddrop_backend.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(
            @RequestBody LoginRequest request) {

        User user = authService.authenticate(
                request.getEmail(),
                request.getPassword()
        );

        if (user == null) {
            return null;
        }

        String token = authService.generateToken(user);

        return new LoginResponse(token, user);
    }
}