package com.payverse.userservice.security;

import com.payverse.userservice.model.User;
import com.payverse.userservice.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtTokenProvider jwtTokenProvider,
                         UserRepository userRepository) {

        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {


        String authHeader = request.getHeader("Authorization");
        System.out.println("Authorization Header: " + authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            String token = authHeader.substring(7);

            if (jwtTokenProvider.validateToken(token)) {

                Claims claims = jwtTokenProvider.extractClaims(token);

                String email = claims.getSubject();
                System.out.println("JWT Email: " + email);

             User user = userRepository
        .findByEmail(email)
        .orElse(null);

System.out.println("User Found: " + user);

if (user != null) {

    UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    List.of(
                            new SimpleGrantedAuthority(
                                    "ROLE_" + user.getRole().name()
                            )
                    )
            );

    SecurityContextHolder.getContext().setAuthentication(authentication);

    System.out.println("Authentication Set: "
            + SecurityContextHolder.getContext().getAuthentication());

            System.out.println(
    "JWT AUTH CREATED : "
    + authentication.getName()
            );
}
            }
        }

        filterChain.doFilter(request, response);
    }
}