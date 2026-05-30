package com.example.emotion_diary_server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
class DiaryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String username;
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        username = "diary" + System.nanoTime();
        token = registerAndGetToken(username);
    }

    @Test
    void diaryEntryCanBeUpsertedAndQueried() throws Exception {
        LocalDate today = LocalDate.now();
        String body = """
                {"entryDate":"%s","moodScore":4,"textNote":"Buen día"}
                """.formatted(today);

        mockMvc.perform(post("/" + username + "/diary/entries")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moodScore").value(4))
                .andExpect(jsonPath("$.textNote").value("Buen día"));

        mockMvc.perform(get("/" + username + "/diary/entries/" + today)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moodScore").value(4));

        String updateBody = """
                {"entryDate":"%s","moodScore":5,"textNote":"Mejor aún"}
                """.formatted(today);

        mockMvc.perform(post("/" + username + "/diary/entries")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moodScore").value(5));
    }

    @Test
    void diaryEntriesCanBeListedInDateRange() throws Exception {
        LocalDate today = LocalDate.now();
        String body = """
                {"entryDate":"%s","moodScore":3,"textNote":"Nota"}
                """.formatted(today);

        mockMvc.perform(post("/" + username + "/diary/entries")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(get("/" + username + "/diary/entries")
                        .param("from", today.minusDays(7).toString())
                        .param("to", today.toString())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].entryDate").value(today.toString()));
    }

    @Test
    void diaryEntryCanBeDeleted() throws Exception {
        LocalDate today = LocalDate.now();
        String body = """
                {"entryDate":"%s","moodScore":2}
                """.formatted(today);

        mockMvc.perform(post("/" + username + "/diary/entries")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/" + username + "/diary/entries/" + today)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/" + username + "/diary/entries/" + today)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void metricsReturnsAggregates() throws Exception {
        LocalDate today = LocalDate.now();
        String body = """
                {"entryDate":"%s","moodScore":4}
                """.formatted(today);

        mockMvc.perform(post("/" + username + "/diary/entries")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(get("/" + username + "/metrics?period=7d")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageMood").value(4.0))
                .andExpect(jsonPath("$.entryStreak").value(1));
    }

    @Test
    void passwordCanBeChanged() throws Exception {
        String body = """
                {"currentPassword":"secret123","newPassword":"newsecret456"}
                """;

        mockMvc.perform(patch("/" + username + "/profile/password")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());
    }

    private String registerAndGetToken(String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"secret123"}
                                """.formatted(name)))
                .andExpect(status().isCreated())
                .andReturn();
        return readToken(result);
    }

    private static String readToken(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        return body.replaceAll(".*\"token\"\\s*:\\s*\"([^\"]+)\".*", "$1");
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
