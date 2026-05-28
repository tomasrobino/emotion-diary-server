package com.example.emotion_diary_server.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MoodboardProperties.class)
public class MoodboardConfig {
}
