package com.yuranium.userservice.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javalab.core.ExceptionBody;
import com.yuranium.userservice.enums.RoleType;
import com.yuranium.userservice.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserActivityFilter extends OncePerRequestFilter
{
    private final UserService userService;

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException
    {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuth))
        {
            filterChain.doFilter(request, response);
            return;
        }

        try
        {
            String keycloakId = jwtAuth.getToken().getSubject();
            if (!isServiceRequest(authentication))
                userService.validateUser(UUID.fromString(keycloakId));
        } catch (Exception e)
        {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(
                    new ExceptionBody(
                            HttpStatus.FORBIDDEN.value(),
                            "User account is disabled or not found"
                    )
            ));
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean isServiceRequest(Authentication authentication)
    {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(RoleType.ROLE_SERVICE.name()));
    }
}