package com.tele.notesapi.service;

import com.tele.notesapi.exception.ResourceNotFoundException;
import com.tele.notesapi.mapper.NoteMapper;
import com.tele.notesapi.model.Constant;
import com.tele.notesapi.model.NoteRequest;
import com.tele.notesapi.model.NoteResponse;
import com.tele.notesapi.model.NoteSummary;
import com.tele.notesapi.model.store.Note;
import com.tele.notesapi.respository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class NoteService {
    private static final Pattern WORD_SPLIT_PATTERN = Pattern.compile("[\\s.,!?;:]+");

    private final NoteRepository noteRepository;
    private final NoteMapper noteMapper;

    @Autowired
    public NoteService(NoteRepository noteRepository, NoteMapper noteMapper) {
        this.noteRepository = noteRepository;
        this.noteMapper = noteMapper;
    }

    public List<NoteResponse> fetchNotes(Set<Constant.Tag> tags, int page, int size) {
        Page<Note> notesPage = getNotesPage(tags, page, size);

        return noteMapper.toResponseDTOList(notesPage.getContent());
    }

    public List<NoteSummary> fetchNoteSummaries(Set<Constant.Tag> tags, int page, int size) {
        Page<Note> notesPage = getNotesPage(tags, page, size);
        return noteMapper.toSummaryDTOList(notesPage.getContent());
    }

    public NoteResponse createNote(NoteRequest noteRequest) {
        return noteMapper.toResponseDTO(noteRepository.save(noteMapper.toEntity(noteRequest)));
    }

    public NoteResponse getNote(UUID id) {
        return noteRepository.findById(id)
                .map(noteMapper::toResponseDTO)
                .orElseThrow(() -> throwResourceNotFoundException(id));
    }

    public NoteResponse updateNote(UUID id, NoteRequest noteRequest) {
        Note existingNote = noteRepository.findById(id)
                .orElseThrow(() -> throwResourceNotFoundException(id));
        existingNote.setTitle(noteRequest.getTitle());
        existingNote.setText(noteRequest.getText());
        existingNote.setTags(noteRequest.getTags());

        Note updatedNote = noteRepository.save(existingNote);
        return noteMapper.toResponseDTO(updatedNote);
    }

    public void deleteNote(UUID id) {
        if (!noteRepository.existsById(id)) {
            throw throwResourceNotFoundException(id);
        }
        noteRepository.deleteById(id);
    }

    public Map<String, Integer> getNoteTextStats(UUID id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> throwResourceNotFoundException(id));
        return calculateWordFrequency(note.getText());
    }

    private ResourceNotFoundException throwResourceNotFoundException(UUID id) {
        return new ResourceNotFoundException(String.format("Note with ID %s not found", id));
    }

    private Page<Note> getNotesPage(Set<Constant.Tag> tags, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());

        return Optional.ofNullable(tags)
                .filter(__ -> !tags.isEmpty())
                .map(__ -> noteRepository.findByTagsIn(tags, pageable))
                .orElseGet(() -> noteRepository.findAll(pageable));
    }

    private Map<String, Integer> calculateWordFrequency(String text) {
        String[] words = WORD_SPLIT_PATTERN.split(text);
        Map<String, Integer> wordFrequencyMap = countWordOccurrences(words);
        return sortWordFrequencyMap(wordFrequencyMap);
    }

    private Map<String, Integer> countWordOccurrences(String[] words) {
        Map<String, Integer> wordFrequencyMap = new LinkedHashMap<>();
        Arrays.stream(words)
                .map(String::toLowerCase)
                .forEach(word -> wordFrequencyMap.put(word, wordFrequencyMap.getOrDefault(word, 0) + 1));
        return wordFrequencyMap;
    }

    private Map<String, Integer> sortWordFrequencyMap(Map<String, Integer> wordFrequencyMap) {
        return wordFrequencyMap.entrySet()
                .stream()
                .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
}
