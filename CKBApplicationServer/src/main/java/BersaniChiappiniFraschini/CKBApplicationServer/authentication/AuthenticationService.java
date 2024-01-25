package BersaniChiappiniFraschini.CKBApplicationServer.authentication;

import BersaniChiappiniFraschini.CKBApplicationServer.config.JwtService;
import BersaniChiappiniFraschini.CKBApplicationServer.user.AccountType;
import BersaniChiappiniFraschini.CKBApplicationServer.user.User;
import BersaniChiappiniFraschini.CKBApplicationServer.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service that deals with authentication actions (Login and Registration)
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository repository;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public ResponseEntity<AuthenticationResponse> register(RegisterRequest request) {
        AccountType account_type;

        //Check if valid account type
        try{
            account_type = AccountType.valueOf(request.getAccount_type());
        }catch (Exception ignored){
            var body = AuthenticationResponse.builder().error_msg("Invalid account type").build();
            return ResponseEntity.badRequest().body(body);
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .accountType(account_type)
                .build();

        if(repository.existsUserByEmail(user.getEmail())){
            var body = AuthenticationResponse.builder().error_msg("Email already used").build();
            return ResponseEntity.badRequest().body(body);
        }

        if(repository.existsUserByUsername(user.getUsername())){
            var body = AuthenticationResponse.builder().error_msg("Username already taken").build();
            return ResponseEntity.badRequest().body(body);
        }

        repository.insert(user);

        String jwt = jwtService.generateJWT(user);
        return ResponseEntity.ok(AuthenticationResponse.builder().token(jwt).build());
    }

    public ResponseEntity<AuthenticationResponse> login(LoginRequest request) {
        var token = new UsernamePasswordAuthenticationToken(
                request.getEmail_or_username(),
                request.getPassword()
        );

        // If auth fails, an exception is thrown and a 403 response is returned
        authenticationManager.authenticate(token);

        // Otherwise, the user is authenticated and the token is generated
        var user = userDetailsService.loadUserByUsername(request.getEmail_or_username());
        String jwt = jwtService.generateJWT(user);
        return ResponseEntity.ok(AuthenticationResponse.builder().token(jwt).build());
    }


}
