package com.javalab.taskservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

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
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/task/**/starter-code").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/task/**/test-case").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/task/**/test-case/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/task/**/test-case/**").hasRole("ADMIN")
                        .anyRequest().permitAll()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }
}