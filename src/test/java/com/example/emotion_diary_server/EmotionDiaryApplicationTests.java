package com.example.emotion_diary_server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"spring.sql.init.mode=never",
		"spring.jpa.defer-datasource-initialization=false"
})
class EmotionDiaryApplicationTests {

	@Test
	void contextLoads() {
	}

}
