package com.yuranium.userservice.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuranium.userservice.util.exception.ExceptionBody;
import com.yuranium.userservice.util.security.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter
{
    public static final String BEARER_PREFIX = "Bearer ";

    public static final String AUTH_HEADER = "Authorization";

    private final JwtUtil jwtUtil;

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException
    {
        String authHeader = request.getHeader(AUTH_HEADER);
        String jwtToken = null, username = null;

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX))
        {
            jwtToken = authHeader.substring(BEARER_PREFIX.length());
            try {
                username = jwtUtil.getUsername(jwtToken);
            } catch (ExpiredJwtException | SignatureException | MalformedJwtException exc) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(objectMapper.writeValueAsString(
                        new ExceptionBody(HttpStatus.UNAUTHORIZED,
                                LocalDateTime.now(),
                                exc.getMessage())
                ));
                return;
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null)
        {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(username, null,
                            List.of(jwtUtil.getRole(jwtToken)));

            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}