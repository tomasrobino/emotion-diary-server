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

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

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
                        .requestMatchers("/quiz/today").authenticated()
                        .requestMatchers("/public/moodboards").authenticated()
                        .requestMatchers("/users/search").authenticated()
                        .requestMatchers("/{user}/diary/**").authenticated()
                        .requestMatchers("/{user}/metrics/**").authenticated()
                        .requestMatchers("/{user}/quiz/**").authenticated()
                        .requestMatchers("/{user}/profile/**").authenticated()
                        .requestMatchers("/{user}/moodboards/**").authenticated()
                        .requestMatchers("/{user}/liked-moodboards").authenticated()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
