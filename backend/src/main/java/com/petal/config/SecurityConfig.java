package com.petal.config;

import com.petal.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        // Product browsing – any authenticated user can read
                        .requestMatchers(HttpMethod.GET, "/api/products/**").authenticated()
                        // Product CUD – only florists
                        .requestMatchers(HttpMethod.POST, "/api/products/**").hasAuthority("ROLE_FLORIST")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasAuthority("ROLE_FLORIST")
                        .requestMatchers(HttpMethod.PATCH, "/api/products/**").hasAuthority("ROLE_FLORIST")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasAuthority("ROLE_FLORIST")
                        // Florist profile endpoints
                        .requestMatchers("/api/florists/**").hasAuthority("ROLE_FLORIST")
                        // Everything else requires authentication
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
