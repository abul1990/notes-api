package com.teletronics.notesapi.mapper;

import com.teletronics.notesapi.model.Constant;
import com.teletronics.notesapi.model.NoteRequest;
import com.teletronics.notesapi.model.NoteResponse;
import com.teletronics.notesapi.model.NoteSummary;
import com.teletronics.notesapi.model.store.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NoteMapperTest {

    private NoteMapper noteMapper;

    @BeforeEach
    public void setUp() {
        noteMapper = Mappers.getMapper(NoteMapper.class);
    }

    @Test
    public void shouldMapGivenNoteRequestToNoteEntity() {
        // Given
        NoteRequest noteRequest = new NoteRequest();
        noteRequest.setTitle("Test Note");
        noteRequest.setText("This is a test note.");
        noteRequest.setTags(Set.of(Constant.Tag.PERSONAL));

        // When
        Note result = noteMapper.toEntity(noteRequest);

        // Then
        assertEquals(noteRequest.getTitle(), result.getTitle());
        assertEquals(noteRequest.getText(), result.getText());
        assertEquals(noteRequest.getTags(), result.getTags());
    }

    @Test
    public void shouldMapGivenNoteEntityToNoteResponseDTO() {
        // Given
        Note note = new Note(UUID.randomUUID(), "Test Note", "This is a test note.", Set.of(Constant.Tag.PERSONAL), null);

        // When
        NoteResponse result = noteMapper.toResponseDTO(note);

        // Then
        assertEquals(note.getTitle(), result.getTitle());
        assertEquals(note.getText(), result.getText());
        assertEquals(note.getTags(), result.getTags());
    }

    @Test
    public void shouldMapToNoteResponseDTOList() {
        // Given
        Note note = new Note(UUID.randomUUID(), "Test Note", "This is a test note.", Set.of(Constant.Tag.PERSONAL), null);
        List<Note> noteList = Collections.singletonList(note);

        // When
        List<NoteResponse> resultList = noteMapper.toResponseDTOList(noteList);

        // Then
        assertEquals(1, resultList.size());
        assertEquals(note.getTitle(), resultList.get(0).getTitle());
        assertEquals(note.getText(), resultList.get(0).getText());
        assertEquals(note.getTags(), resultList.get(0).getTags());
    }

    @Test
    public void shouldMapToSummaryDTO() {
        // Given
        Note note = new Note(UUID.randomUUID(), "Test Note", "This is a test note.", Set.of(Constant.Tag.PERSONAL), null);

        // When
        NoteSummary result = noteMapper.toSummaryDTO(note);

        // Then
        assertEquals(note.getTitle(), result.getTitle());
        assertEquals(note.getCreatedDate(), result.getCreatedDate());
    }

    @Test
    public void shouldMapToSummaryDTOList() {
        // Given
        Note note = new Note(UUID.randomUUID(), "Test Note", "This is a test note.", Set.of(Constant.Tag.PERSONAL), null);
        List<Note> noteList = Collections.singletonList(note);

        // When
        List<NoteSummary> resultList = noteMapper.toSummaryDTOList(noteList);

        // Then
        assertEquals(1, resultList.size());
        assertEquals(note.getTitle(), resultList.get(0).getTitle());
        assertEquals(note.getCreatedDate(), resultList.get(0).getCreatedDate());
    }
}
