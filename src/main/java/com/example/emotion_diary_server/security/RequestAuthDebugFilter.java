package com.example.emotion_diary_server.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestAuthDebugFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestAuthDebugFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String authScheme = "<none>";
        if (authHeader != null && !authHeader.isBlank()) {
            int spaceIndex = authHeader.indexOf(' ');
            authScheme = (spaceIndex > 0) ? authHeader.substring(0, spaceIndex) : authHeader;
        }

        log.info("AUTH DEBUG -> {} {} | authorizationHeaderPresent={} | scheme={}",
                request.getMethod(),
                request.getRequestURI(),
                authHeader != null,
                authScheme);

        filterChain.doFilter(request, response);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("AUTH DEBUG <- {} {} | status={} | principal={} | authenticated={}",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                authentication != null ? authentication.getName() : "<none>",
                authentication != null && authentication.isAuthenticated());
    }
}
