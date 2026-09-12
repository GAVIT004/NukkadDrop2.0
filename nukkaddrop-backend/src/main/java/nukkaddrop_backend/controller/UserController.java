package nukkaddrop_backend.controller;

import nukkaddrop_backend.entity.User;
import nukkaddrop_backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .orElse(null);
    }

    @GetMapping("/shopkeeper-test")
    public String shopkeeperTest() {
        return "SHOPKEEPER access granted";
    }

    @PostMapping
    public User createUser(@RequestBody User user) {

        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        return userRepository.save(user);
    }

    @PutMapping("/{id}")
    public User updateUser(
            @PathVariable Long id,
            @RequestBody User updatedUser) {

        return userRepository.findById(id)
                .map(user -> {

                    user.setName(updatedUser.getName());
                    user.setEmail(updatedUser.getEmail());

                    if (updatedUser.getPassword() != null &&
                            !updatedUser.getPassword().isBlank()) {

                        user.setPassword(
                                passwordEncoder.encode(
                                        updatedUser.getPassword()
                                )
                        );
                    }

                    user.setRole(updatedUser.getRole());

                    return userRepository.save(user);
                })
                .orElse(null);
    }

    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {

        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return "User deleted successfully";
        }

        return "User not found";
    }
}