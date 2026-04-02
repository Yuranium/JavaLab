package com.javalab.taskservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig
{
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
    {
        return http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/v1/task").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/task/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/task/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/task/category").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/task/category").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/task/category").hasRole("ADMIN")
                        .anyRequest().permitAll()
                )
                .oauth2ResourceServer(auth -> auth.jwt(Customizer.withDefaults()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter()
    {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setPrincipalClaimName("sub");

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess == null) return Collections.emptySet();

            Collection<String> roles = (Collection<String>) realmAccess.get("roles");
            if (roles == null) return Collections.emptySet();

            return roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet());
        });

        return converter;
    }
}