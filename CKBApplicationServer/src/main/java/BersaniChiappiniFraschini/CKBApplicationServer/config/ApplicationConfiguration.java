package BersaniChiappiniFraschini.CKBApplicationServer.config;

import BersaniChiappiniFraschini.CKBApplicationServer.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.regex.Pattern;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfiguration {
    private final UserRepository userRepository;
    @Bean
    public UserDetailsService userDetailsService(){
        return username -> {
            if(isEmail(username)){
                return userRepository
                        .findUserByEmail(username)
                        .orElseThrow(() ->
                                new UsernameNotFoundException("User with email %s not found".formatted(username)));
            }else{
                return userRepository
                        .findUserByUsername(username)
                        .orElseThrow(() ->
                                new UsernameNotFoundException("User with username %s not found".formatted(username)));
            }
        };
    }

    //TODO change this when auth microservice is up and running
    @Bean
    public AuthenticationProvider authProvider(){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager(); // I dunno why provider and manager are two separate things but ok
    }

    private boolean isEmail(String string) {
        String regex = "^(.+)@(.+)$";
        return Pattern.compile(regex).matcher(string).matches();
    }
}
