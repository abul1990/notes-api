package com.teletronics.notesapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.teletronics.notesapi.model.Constant;
import com.teletronics.notesapi.model.NoteRequest;
import com.teletronics.notesapi.model.NoteResponse;
import com.teletronics.notesapi.model.NoteSummary;
import com.teletronics.notesapi.service.NoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotesControllerTest {

    @Mock
    private NoteService noteService;

    @InjectMocks
    private NotesController notesController;

    private MockMvc mockMvc;

    ObjectMapper objectMapper = new ObjectMapper();


    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notesController).build();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void shouldFetchNoteSummariesForGivenQueryParams() throws Exception {
        NoteSummary summary = new NoteSummary();
        summary.setTitle("Test Note");
        summary.setCreatedDate(LocalDateTime.now());
        when(noteService.fetchNoteSummaries(any(Set.class), any(Integer.class), any(Integer.class)))
                .thenReturn(Collections.singletonList(summary));

        mockMvc.perform(get("/api/v1/notes")
                        .param("tags", Constant.Tag.PERSONAL.name())
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Note"))
                .andExpect(jsonPath("$[0].createdDate").isNotEmpty());
    }

    @Test
    void shouldCreateNoteForGivenRequest() throws Exception {
        NoteRequest noteRequest = new NoteRequest();
        noteRequest.setTitle("Test Note");
        noteRequest.setText("This is a test note.");
        noteRequest.setCreatedDate(LocalDateTime.now());
        noteRequest.setTags(Set.of(Constant.Tag.PERSONAL));

        NoteResponse createdNoteResponse = new NoteResponse();
        createdNoteResponse.setTitle("Test Note");
        createdNoteResponse.setText("This is a test note.");
        when(noteService.createNote(any(NoteRequest.class)))
                .thenReturn(createdNoteResponse);

        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noteRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Note"))
                .andExpect(jsonPath("$.text").value("This is a test note."));
    }

    @Test
    void shouldReturnBadRequestWhenTextIsNull() throws Exception {
        NoteRequest noteRequest = new NoteRequest();
        noteRequest.setTitle("Test Note");
        noteRequest.setText(null);
        noteRequest.setCreatedDate(LocalDateTime.now());
        noteRequest.setTags(Set.of(Constant.Tag.PERSONAL));

        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noteRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenTitleIsBlank() throws Exception {
        NoteRequest noteRequest = new NoteRequest();
        noteRequest.setTitle("");
        noteRequest.setText("This is a test note.");
        noteRequest.setCreatedDate(LocalDateTime.now());
        noteRequest.setTags(Set.of(Constant.Tag.PERSONAL));

        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noteRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetNoteById() throws Exception {
        UUID id = UUID.randomUUID();
        NoteResponse expectedResponse = new NoteResponse();
        expectedResponse.setTitle("Test Note");
        expectedResponse.setText("This is a test note.");
        when(noteService.getNote(id)).thenReturn(expectedResponse);

        mockMvc.perform(get("/api/v1//notes/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Note"));
    }

    @Test
    void shouldUpdateNoteById() throws Exception {
        UUID id = UUID.randomUUID();
        NoteRequest noteRequest = new NoteRequest();
        noteRequest.setTitle("Updated Note");
        noteRequest.setText("This is an updated note.");
        noteRequest.setTags(Set.of(Constant.Tag.BUSINESS));

        NoteResponse noteResponse = new NoteResponse();
        noteResponse.setTitle("Updated Note");
        noteResponse.setText("This is an updated note.");
        when(noteService.updateNote(any(UUID.class), any(NoteRequest.class)))
                .thenReturn(noteResponse);

        mockMvc.perform(put("/api/v1/notes/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(noteRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Note"));
    }

    @Test
    void shouldDeleteNoteById() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(delete("/api/v1/notes/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldGetStatsByNoteId() throws Exception {
        UUID id = UUID.randomUUID();
        Map<String, Integer> expectedStats = Map.of(
                "note", 2,
                "is", 1,
                "just", 1,
                "a", 1
        );
        
        when(noteService.getNoteTextStats(id)).thenReturn(expectedStats);

        mockMvc.perform(get("/api/v1/notes/{id}/stats", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note").value(2))
                .andExpect(jsonPath("$.is").value(1))
                .andExpect(jsonPath("$.just").value(1))
                .andExpect(jsonPath("$.a").value(1));
    }
}