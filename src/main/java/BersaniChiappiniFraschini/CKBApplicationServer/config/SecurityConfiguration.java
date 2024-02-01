package BersaniChiappiniFraschini.CKBApplicationServer.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * HTTP security configuration of the server.
 */
@Configuration
@RequiredArgsConstructor
@EnableWebSecurity // Used to expose the filterChain bean
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable) // to avoid any kind of
                // .cors(AbstractHttpConfigurer::disable) // problem with cors exceptions
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers("/auth/**", "/github/push").permitAll()) // Do not authenticate auth requests
                .authorizeHttpRequests(auth ->
                        auth.anyRequest().authenticated()) // Authenticate everything else
                .sessionManagement(config ->
                        config.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // No need to store the session
                .authenticationProvider(authenticationProvider) // Set the authentication provider
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class) // First check JWT and then the password
                .build();
    }
}
