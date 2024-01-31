package BersaniChiappiniFraschini.CKBApplicationServer.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that must be applied once per request for user authentication and JWT validation.
 * It works as a middleware to be executed before a request is dispatched by the DispatcherServlet.
 * */
@Component
@RequiredArgsConstructor // Creates a constructor for all final (hence "required") fields
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Check for Authorization field in header
        final String authHeader = request.getHeader("Authorization");
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }

        // Get JWT and extract username
        final String jwt = authHeader.substring(7); //Skip first 7 chars ("Bearer ")
        final String username = jwtService.extractUsername(jwt);

        // If user is not authenticated
        if(username != null && SecurityContextHolder.getContext().getAuthentication() == null){

            // If user is not found, this throws a UsernameNotFoundException.
            // The exception is caught by the filter and returned as a 403 response.
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if(jwtService.isValid(jwt, userDetails)){
                // If token is valid, update the security context
                UsernamePasswordAuthenticationToken pwdAuthToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // no need for credentials
                        userDetails.getAuthorities()
                );

                pwdAuthToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(pwdAuthToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
