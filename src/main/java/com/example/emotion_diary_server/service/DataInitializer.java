package com.example.emotion_diary_server.service;

import com.example.emotion_diary_server.model.QuizTemplate;
import com.example.emotion_diary_server.repository.QuizTemplateRepository;
import com.example.emotion_diary_server.user.User;
import com.example.emotion_diary_server.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final QuizTemplateRepository quizTemplateRepository;

    public DataInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            QuizTemplateRepository quizTemplateRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.quizTemplateRepository = quizTemplateRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User user = new User();
            user.setUsername("admin");
            user.setPassword(Objects.requireNonNull(passwordEncoder.encode("admin123")));
            userRepository.save(user);
            System.out.println("Default user created: admin / admin123");
        }
        seedQuizTemplates();
    }

    private void seedQuizTemplates() {
        if (quizTemplateRepository.count() > 0) {
            return;
        }
        quizTemplateRepository.save(createTemplate(
                "¿Cómo te sientes hoy en general?",
                "scale",
                "[\"1\",\"2\",\"3\",\"4\",\"5\"]",
                1
        ));
        quizTemplateRepository.save(createTemplate(
                "¿Qué emoción predomina hoy?",
                "choice",
                "[\"Alegría\",\"Tristeza\",\"Ansiedad\",\"Calma\",\"Enfado\"]",
                2
        ));
        quizTemplateRepository.save(createTemplate(
                "¿Has dormido bien anoche?",
                "choice",
                "[\"Sí, muy bien\",\"Regular\",\"Mal\",\"Muy mal\"]",
                3
        ));
    }

    private static QuizTemplate createTemplate(String question, String type, String options, int sortOrder) {
        QuizTemplate template = new QuizTemplate();
        template.setQuestion(question);
        template.setType(type);
        template.setOptions(options);
        template.setSortOrder(sortOrder);
        return template;
    }
}