package com.tele.notesapi.integration;

import com.tele.notesapi.model.NoteResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class NotesControllerIntegrationTest {

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.3");

    @Autowired
    private WebTestClient webTestClient;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeAll
    static void beforeAll() {
        mongoDBContainer.start();
    }

    @AfterAll
    static void afterAll() {
        mongoDBContainer.stop();
    }

    @Test
    public void shouldCreateNoteWithValidPayload() {
        String newNote = """
                {
                  "title": "Test Note",
                  "text": "This is a test note.",
                  "tag": "PERSONAL",
                  "createdDate": "2024-07-02T10:36:00"
                }
                """;
        webTestClient.post().uri("/api/v1/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newNote)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Test Note")
                .jsonPath("$.text").isEqualTo("This is a test note.");
    }

    @Test
    public void shouldReturnNotesWhenFetchedWithValidQueryParams() {
        webTestClient.get().uri(uriBuilder ->
                        uriBuilder.path("/api/v1/notes")
                                .queryParam("tags", "PERSONAL")
                                .queryParam("page", "0")
                                .queryParam("size", "10")
                                .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray();
    }

    @Test
    public void shouldReturnNoteStatsForExistingNote() {
        // Create a new note
        String newNote = """
                {
                  "title": "Test Note",
                  "text": "note is just a note",
                  "tag": "PERSONAL",
                  "createdDate": "2024-07-02T10:36:00"
                }
                """;
        NoteResponse noteResponse = webTestClient.post().uri("/api/v1/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newNote)
                .exchange()
                .expectStatus().isCreated()
                .returnResult(NoteResponse.class).getResponseBody().blockFirst();

        // Fetch the note stats
        webTestClient.get().uri("api/v1/notes/" + noteResponse.getId() + "/stats")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.note").isEqualTo(2)
                .jsonPath("$.is").isEqualTo(1)
                .jsonPath("$.just").isEqualTo(1)
                .jsonPath("$.a").isEqualTo(1);
    }

    @Test
    public void shouldReturnBadRequestForInvalidPayload() {
        String newNote = """
                {
                  "title": "",
                  "text": "This is a test note.",
                  "tag": "PERSONAL",
                  "createdDate": "2024-07-02T10:36:00"
                }
                """;
        webTestClient.post().uri("/api/v1/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newNote)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    public void shouldReturnNotFoundWhenNoteIdDoesNotExist() {
        webTestClient.get().uri("/api/v1/notes/" + UUID.randomUUID() + "/stats")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }
}

