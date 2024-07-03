package com.tele.notes_api.service;

import com.tele.notes_api.exception.ResourceNotFoundException;
import com.tele.notes_api.mapper.NoteMapper;
import com.tele.notes_api.model.Constant;
import com.tele.notes_api.model.NoteRequest;
import com.tele.notes_api.model.NoteResponse;
import com.tele.notes_api.model.NoteSummary;
import com.tele.notes_api.model.store.Note;
import com.tele.notes_api.respository.NoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private NoteMapper noteMapper;

    @InjectMocks
    private NoteService noteService;

    @Test
    void shouldCreateNoteWhenRequestIsValid() {
        // Given
        NoteRequest request = new NoteRequest();
        request.setTitle("Test Note");
        request.setText("This is a test note.");
        request.setTag(Constant.Tag.PERSONAL);

        Note noteEntity = new Note();
        noteEntity.setTitle(request.getTitle());
        noteEntity.setText(request.getText());
        noteEntity.setTag(request.getTag());

        NoteResponse expectedResponse = new NoteResponse();
        expectedResponse.setTitle(request.getTitle());
        expectedResponse.setText(request.getText());
        expectedResponse.setTag(request.getTag());

        // Mocking behavior
        when(noteMapper.toEntity(any(NoteRequest.class))).thenReturn(noteEntity);
        when(noteRepository.save(any(Note.class))).thenReturn(noteEntity);
        when(noteMapper.toResponseDTO(any(Note.class))).thenReturn(expectedResponse);

        // When
        NoteResponse actualResponse = noteService.createNote(request);

        // Then
        assertEquals(expectedResponse.getTitle(), actualResponse.getTitle());
        assertEquals(expectedResponse.getText(), actualResponse.getText());
        assertEquals(expectedResponse.getTag(), actualResponse.getTag());

        verify(noteMapper, times(1)).toEntity(any(NoteRequest.class));
        verify(noteRepository, times(1)).save(any(Note.class));
        verify(noteMapper, times(1)).toResponseDTO(any(Note.class));
    }

    @Test
    void shouldFetchAllNotes() {
        // Given
        Note note = new Note(UUID.randomUUID(), "Test Note 1", "Text 1", Constant.Tag.PERSONAL, LocalDateTime.now());
        Note anotherNote = new Note(UUID.randomUUID(), "Test Note 2", "Text 2", Constant.Tag.BUSINESS, LocalDateTime.now());
        List<Note> notes = List.of(note, anotherNote);

        List<NoteResponse> expectedResponse = Arrays.asList(
                new NoteResponse(note.getId(), note.getTitle(), note.getText(), note.getTag(), note.getCreatedDate()),
                new NoteResponse(anotherNote.getId(), anotherNote.getTitle(), anotherNote.getText(), anotherNote.getTag(), anotherNote.getCreatedDate())
        );

        when(noteRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(notes));
        when(noteMapper.toResponseDTOList(anyList())).thenReturn(expectedResponse);

        // When
        List<NoteResponse> actualResponse = noteService.fetchNotes(0, 10);

        // Then
        assertEquals(2, actualResponse.size());
        assertEquals(expectedResponse.get(0).getTitle(), actualResponse.get(0).getTitle());
        assertEquals(expectedResponse.get(1).getText(), actualResponse.get(1).getText());

        verify(noteRepository, times(1)).findAll(any(Pageable.class));
        verify(noteMapper, times(1)).toResponseDTOList(anyList());
    }

    @Test
    void shouldFetchNoteSummariesForGivenTags() {
        // Given
        Set<Constant.Tag> tags = new HashSet<>();
        tags.add(Constant.Tag.PERSONAL);
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());

        List<Note> notes = List.of(new Note(UUID.randomUUID(), "Test Note", "Text", Constant.Tag.PERSONAL, LocalDateTime.now()));
        Page<Note> notesPage = new org.springframework.data.domain.PageImpl<>(notes, pageable, notes.size());
        List<NoteSummary> expectedSummaries = List.of(new NoteSummary("Test Note", LocalDateTime.now()));

        when(noteRepository.findByTagIn(eq(tags), eq(pageable))).thenReturn(notesPage);
        when(noteMapper.toSummaryDTOList(anyList())).thenReturn(expectedSummaries);

        // When
        List<NoteSummary> actualSummaries = noteService.fetchNoteSummaries(tags, page, size);

        // Then
        assertEquals(1, actualSummaries.size());
        assertEquals(expectedSummaries.get(0).getTitle(), actualSummaries.get(0).getTitle());

        verify(noteRepository, times(1)).findByTagIn(eq(tags), eq(pageable));
        verify(noteMapper, times(1)).toSummaryDTOList(anyList());
    }

    @Test
    void shouldFetchNoteSummariesWithoutTags() {
        // Given
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());

        List<Note> notes = Arrays.asList(
                new Note(UUID.randomUUID(), "Test Note 1", "Text 1", Constant.Tag.PERSONAL, LocalDateTime.now()),
                new Note(UUID.randomUUID(), "Test Note 2", "Text 2", Constant.Tag.BUSINESS, LocalDateTime.now())
        );
        Page<Note> notesPage = new org.springframework.data.domain.PageImpl<>(notes, pageable, notes.size());
        List<NoteSummary> expectedSummaries = Arrays.asList(
                new NoteSummary("Test Note 1", LocalDateTime.now()),
                new NoteSummary("Test Note 2", LocalDateTime.now())
        );

        // Mocking behavior
        when(noteRepository.findAll(eq(pageable))).thenReturn(notesPage);
        when(noteMapper.toSummaryDTOList(anyList())).thenReturn(expectedSummaries);

        // When
        List<NoteSummary> actualSummaries = noteService.fetchNoteSummaries(null, page, size);

        // Then
        assertEquals(2, actualSummaries.size());
        assertEquals(expectedSummaries.get(0).getTitle(), actualSummaries.get(0).getTitle());

        verify(noteRepository, times(1)).findAll(eq(pageable));
        verify(noteMapper, times(1)).toSummaryDTOList(anyList());
    }

    @Test
    void shouldReturnNoteById() {
        // Given
        UUID id = UUID.randomUUID();
        NoteResponse expectedResponse = new NoteResponse(id, "Test Note", "Text", Constant.Tag.PERSONAL, LocalDateTime.now());

        // Mocking behavior
        when(noteRepository.findById(eq(id))).thenReturn(Optional.of(new Note(
                id, "Test Note", "Text", Constant.Tag.PERSONAL, LocalDateTime.now())));
        when(noteMapper.toResponseDTO(any(Note.class))).thenReturn(expectedResponse);

        // When
        NoteResponse actualResponse = noteService.getNote(id);

        // Then
        assertEquals(expectedResponse.getTitle(), actualResponse.getTitle());
        assertEquals(expectedResponse.getText(), actualResponse.getText());

        verify(noteRepository, times(1)).findById(eq(id));
        verify(noteMapper, times(1)).toResponseDTO(any(Note.class));
    }

    @Test
    void shouldUpdateNoteById() {
        // Given
        UUID id = UUID.randomUUID();
        NoteRequest request = new NoteRequest();
        request.setTitle("Updated Note");
        request.setText("Updated Text");
        request.setTag(Constant.Tag.BUSINESS);

        Note existingNote = new Note(id, "Test Note", "Text", Constant.Tag.PERSONAL, LocalDateTime.now());
        Note updatedNote = new Note(id, "Updated Note", "Updated Text", Constant.Tag.BUSINESS, LocalDateTime.now());
        NoteResponse expectedResponse = new NoteResponse(id, "Updated Note", "Updated Text", Constant.Tag.BUSINESS, LocalDateTime.now());

        // Mocking behavior
        when(noteRepository.findById(eq(id))).thenReturn(Optional.of(existingNote));
        when(noteRepository.save(any(Note.class))).thenReturn(updatedNote);
        when(noteMapper.toResponseDTO(any(Note.class))).thenReturn(expectedResponse);

        // When
        NoteResponse actualResponse = noteService.updateNote(id, request);

        // Then
        assertEquals(expectedResponse.getTitle(), actualResponse.getTitle());
        assertEquals(expectedResponse.getText(), actualResponse.getText());
        assertEquals(expectedResponse.getTag(), actualResponse.getTag());

        verify(noteRepository, times(1)).findById(eq(id));
        verify(noteRepository, times(1)).save(any(Note.class));
        verify(noteMapper, times(1)).toResponseDTO(any(Note.class));
    }

    @Test
    void shouldDeleteNoteById() {
        // Given
        UUID id = UUID.randomUUID();

        // Mocking behavior
        when(noteRepository.existsById(eq(id))).thenReturn(true);

        // When
        noteService.deleteNote(id);

        // Then
        verify(noteRepository, times(1)).existsById(eq(id));
        verify(noteRepository, times(1)).deleteById(eq(id));
    }

    @Test
    public void shouldGetTextStatsForGivenNoteId() {
        // Given
        UUID noteId = UUID.randomUUID();
        Note note = new Note();
        note.setId(noteId);
        note.setTitle("Title 1");
        note.setText("note is just a note");
        Map<String, Integer> expectedStats = Map.of(
                "note", 2,
                "is", 1,
                "just", 1,
                "a", 1
        );

        when(noteRepository.findById(eq(noteId))).thenReturn(Optional.of(note));


        // When
        Map<String, Integer> result = noteService.getNoteTextStats(noteId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedStats);

        verify(noteRepository, times(1)).findById(noteId);
    }

    @Test
    void shouldThrowExceptionWhenNoteNotFound() {
        // Given
        UUID id = UUID.randomUUID();

        // Mocking behavior
        when(noteRepository.existsById(eq(id))).thenReturn(false);

        // When, Then
        assertThrows(ResourceNotFoundException.class, () -> noteService.deleteNote(id));

        verify(noteRepository, times(1)).existsById(eq(id));
        verify(noteRepository, never()).deleteById(any(UUID.class));
    }
}
