package com.example.emotion_diary_server.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Applies CORS rules from {@link CorsProperties} to MVC and the security filter chain.
 */
@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class WebConfig implements WebMvcConfigurer {

    private final CorsProperties corsProperties;

    /**
     * @param corsProperties bound CORS settings
     */
    public WebConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    /**
     * Registers global CORS mappings when at least one origin or pattern is configured.
     *
     * @param registry Spring MVC CORS registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        CorsConfiguration configuration = buildCorsConfiguration();
        if (configuration.getAllowedOrigins() == null && configuration.getAllowedOriginPatterns() == null) {
            return;
        }

        var mapping = registry.addMapping("/**")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type")
                .maxAge(3600);

        if (configuration.getAllowedOrigins() != null) {
            mapping.allowedOrigins(configuration.getAllowedOrigins().toArray(String[]::new));
        }
        if (configuration.getAllowedOriginPatterns() != null) {
            mapping.allowedOriginPatterns(configuration.getAllowedOriginPatterns().toArray(String[]::new));
        }
    }

    /**
     * Exposes the same CORS configuration for Spring Security.
     *
     * @return CORS configuration source for all paths
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", buildCorsConfiguration());
        return source;
    }

    private CorsConfiguration buildCorsConfiguration() {
        CorsConfiguration configuration = new CorsConfiguration();
        String[] origins = corsProperties.allowedOriginsArray();
        String[] patterns = corsProperties.allowedOriginPatternsArray();

        if (origins.length > 0) {
            configuration.setAllowedOrigins(List.of(origins));
        }
        if (patterns.length > 0) {
            configuration.setAllowedOriginPatterns(List.of(patterns));
        }

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setMaxAge(3600L);
        return configuration;
    }
}
