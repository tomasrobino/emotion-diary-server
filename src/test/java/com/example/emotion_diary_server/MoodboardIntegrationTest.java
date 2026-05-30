package com.example.emotion_diary_server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.defer-datasource-initialization=false"
})
class MoodboardIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String ownerUsername;
    private String otherUsername;
    private String ownerToken;
    private String otherToken;

    @BeforeEach
    void setUp() throws Exception {
        ownerUsername = "owner" + System.nanoTime();
        otherUsername = "other" + System.nanoTime();
        ownerToken = registerAndGetToken(ownerUsername);
        otherToken = registerAndGetToken(otherUsername);
    }

    @Test
    void publicMoodboardCanBeReadWithoutAuthentication() throws Exception {
        long moodboardId = createMoodboard(ownerToken);

        mockMvc.perform(put("/" + ownerUsername + "/moodboards/" + moodboardId + "/visibility?isPublic=true")
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/" + ownerUsername + "/moodboards/" + moodboardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.public").value(true));
    }

    @Test
    void privateMoodboardIsHiddenFromAnonymousUsers() throws Exception {
        long moodboardId = createMoodboard(ownerToken);

        mockMvc.perform(get("/" + ownerUsername + "/moodboards/" + moodboardId))
                .andExpect(status().isNotFound());
    }

    @Test
    void ownerCanListGrantedPermissions() throws Exception {
        long moodboardId = createMoodboard(ownerToken);

        mockMvc.perform(post("/" + ownerUsername + "/moodboards/" + moodboardId + "/permissions?grantTo=" + otherUsername)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/" + ownerUsername + "/moodboards/" + moodboardId + "/permissions")
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(otherUsername));
    }

    @Test
    void grantedUserCanAccessPrivateMoodboard() throws Exception {
        long moodboardId = createMoodboard(ownerToken);

        mockMvc.perform(post("/" + ownerUsername + "/moodboards/" + moodboardId + "/permissions?grantTo=" + otherUsername)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/" + ownerUsername + "/moodboards/" + moodboardId)
                        .header("Authorization", bearer(otherToken)))
                .andExpect(status().isOk());
    }

    @Test
    void mediaUploadRejectsNonImageContentType() throws Exception {
        long moodboardId = createMoodboard(ownerToken);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "clip.txt",
                "text/plain",
                "hello".getBytes()
        );

        mockMvc.perform(multipart("/" + ownerUsername + "/moodboards/" + moodboardId + "/media")
                        .file(file)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isUnsupportedMediaType());
    }

    private String registerAndGetToken(String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"secret1"}
                                """.formatted(username)))
                .andExpect(status().isCreated())
                .andReturn();
        return readToken(result);
    }

    private long createMoodboard(String token) throws Exception {
        MvcResult result = mockMvc.perform(post("/" + ownerUsername + "/moodboards")
                        .header("Authorization", bearer(token))
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

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private static String readToken(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        return body.replaceAll(".*\"token\"\\s*:\\s*\"([^\"]+)\".*", "$1");
    }
}
