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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
class AccountDeletionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String username;
    private String otherUsername;
    private String token;
    private String otherToken;

    @BeforeEach
    void setUp() throws Exception {
        username = "deluser" + System.nanoTime();
        otherUsername = "other" + System.nanoTime();
        token = registerAndGetToken(username);
        otherToken = registerAndGetToken(otherUsername);
    }

    @Test
    void accountCanBeDeletedWithPasswordAndCascadesData() throws Exception {
        LocalDate today = LocalDate.now();
        mockMvc.perform(post("/" + username + "/diary/entries")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"entryDate":"%s","moodScore":4,"textNote":"Nota"}
                                """.formatted(today)))
                .andExpect(status().isOk());

        long moodboardId = createMoodboard(username, token);

        long otherMoodboardId = createMoodboard(otherUsername, otherToken);
        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/" + otherUsername + "/moodboards/" + otherMoodboardId + "/visibility?isPublic=true")
                        .header("Authorization", bearer(otherToken)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/" + otherUsername + "/moodboards/" + otherMoodboardId + "/likes")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/" + otherUsername + "/moodboards/" + otherMoodboardId + "/permissions?grantTo=" + username)
                        .header("Authorization", bearer(otherToken)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/" + username + "/profile")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"password":"secret123"}
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/" + username + "/moodboards/" + moodboardId))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"secret123"}
                                """.formatted(username)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accountDeletionRejectsWrongPassword() throws Exception {
        mockMvc.perform(delete("/" + username + "/profile")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"password":"wrongpassword"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accountDeletionRequiresAuthentication() throws Exception {
        mockMvc.perform(delete("/" + username + "/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"password":"secret123"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCannotDeleteAnotherUsersAccount() throws Exception {
        mockMvc.perform(delete("/" + otherUsername + "/profile")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"password":"secret123"}
                                """))
                .andExpect(status().isForbidden());
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

    private long createMoodboard(String owner, String ownerToken) throws Exception {
        MvcResult result = mockMvc.perform(post("/" + owner + "/moodboards")
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Test board",
                                  "content": {
                                    "version": 1,
                                    "elements": [
                                      {
                                        "id": "fabric-main",
                                        "type": "fabric",
                                        "fabricJson": {"version":"7.0.0","objects":[]}
                                      }
                                    ]
                                  }
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        return Long.parseLong(body.replaceAll(".*\"id\"\\s*:\\s*(\\d+).*", "$1"));
    }

    private static String readToken(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        return body.replaceAll(".*\"token\"\\s*:\\s*\"([^\"]+)\".*", "$1");
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
