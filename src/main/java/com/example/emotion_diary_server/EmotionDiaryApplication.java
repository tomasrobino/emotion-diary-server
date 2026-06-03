package com.example.emotion_diary_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot entry point for the Emotion Diary server.
 */
@SpringBootApplication
@EnableScheduling
public class EmotionDiaryApplication {

	/**
	 * Starts the application context.
	 *
	 * @param args command-line arguments passed to Spring Boot
	 */
	public static void main(String[] args) {
		SpringApplication.run(EmotionDiaryApplication.class, args);
	}

}
