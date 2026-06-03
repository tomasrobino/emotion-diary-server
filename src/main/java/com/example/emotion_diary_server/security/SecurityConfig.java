package com.example.emotion_diary_server.security;

import com.example.emotion_diary_server.config.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security configuration for stateless JWT authentication, CORS, and HTTP authorization rules.
 */
@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    /**
     * Builds the HTTP security filter chain with JWT authentication and route authorization rules.
     *
     * @param http                      HTTP security builder
     * @param jwtAuthFilter             filter that validates Bearer tokens
     * @param corsConfigurationSource   CORS configuration for cross-origin requests
     * @return configured security filter chain
     */
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthFilter,
            CorsConfigurationSource corsConfigurationSource
    ) {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login", "/auth/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/{user}/moodboards/{moodboardId}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/{user}/moodboards/{moodboardId}/thumbnail").permitAll()
                        .requestMatchers(HttpMethod.GET, "/{user}/moodboards/{moodboardId}/media/{assetId}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/{user}/moodboards/{moodboardId}/likes").permitAll()
                        .requestMatchers(HttpMethod.GET, "/{user}/moodboards/{moodboardId}/likes/count").permitAll()
                        .requestMatchers("/auth/logout").authenticated()
                        .requestMatchers("/public/moodboards").authenticated()
                        .requestMatchers("/users/search").authenticated()
                        .requestMatchers("/{user}/diary/**").authenticated()
                        .requestMatchers("/{user}/metrics/**").authenticated()
                        .requestMatchers("/{user}/profile/**").authenticated()
                        .requestMatchers("/{user}/moodboards/**").authenticated()
                        .requestMatchers("/{user}/liked-moodboards").authenticated()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Exposes the Spring {@link AuthenticationManager} for programmatic authentication.
     *
     * @param config authentication configuration
     * @return shared authentication manager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }

    /**
     * Password encoder used for hashing and verifying user credentials.
     *
     * @return BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
