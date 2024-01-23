package BersaniChiappiniFraschini.CKBApplicationServer.authentication;

import BersaniChiappiniFraschini.CKBApplicationServer.config.JwtService;
import BersaniChiappiniFraschini.CKBApplicationServer.user.User;
import BersaniChiappiniFraschini.CKBApplicationServer.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository repository;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .accountType(request.getAccount_type())
                .build();

        repository.insert(user);

        String jwt = jwtService.generateJWT(user);
        return AuthenticationResponse.builder().token(jwt).build();
    }

    public AuthenticationResponse login(LoginRequest request) {
        var token = new UsernamePasswordAuthenticationToken(
                request.getEmail_or_username(),
                request.getPassword()
        );

        // If auth fails, an exception is thrown and a 403 response is returned
        authenticationManager.authenticate(token);

        // Otherwise, the user is authenticated and the token is generated
        var user = userDetailsService.loadUserByUsername(request.getEmail_or_username());
        String jwt = jwtService.generateJWT(user);
        return AuthenticationResponse.builder().token(jwt).build();
    }


}
