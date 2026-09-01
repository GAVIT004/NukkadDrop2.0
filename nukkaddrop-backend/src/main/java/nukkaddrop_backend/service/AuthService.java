package nukkaddrop_backend.service;

import nukkaddrop_backend.entity.User;
import nukkaddrop_backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,PasswordEncoder passwordEncoder){
        this.userRepository=userRepository;
        this.passwordEncoder=passwordEncoder;
    }

    public User authenticate(String email,String password){

        User user = userRepository.findByEmail(email)
                .orElse(null);

        if (user==null){
            return null;
        }

        if (!passwordEncoder.matches(password, user.getPassword())){
            return null;
        }

        return user;
    }
}
