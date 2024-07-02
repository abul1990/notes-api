package com.tele.notes_api.service;

import com.tele.notes_api.exception.ResourceNotFoundException;
import com.tele.notes_api.mapper.NoteMapper;
import com.tele.notes_api.model.Constant;
import com.tele.notes_api.model.NoteRequest;
import com.tele.notes_api.model.NoteResponse;
import com.tele.notes_api.model.NoteSummary;
import com.tele.notes_api.model.store.Note;
import com.tele.notes_api.respository.NoteRepository;
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
    private static final Pattern WORD_SPLIT_PATTERN = Pattern.compile("\\s+");

    private final NoteRepository noteRepository;
    private final NoteMapper noteMapper;

    @Autowired
    public NoteService(NoteRepository noteRepository, NoteMapper noteMapper) {
        this.noteRepository = noteRepository;
        this.noteMapper = noteMapper;
    }

    public List<NoteResponse> fetchNotes() {
        return noteMapper.toResponseDTOList(noteRepository.findAll());
    }

    public List<NoteSummary> fetchNoteSummaries(Set<Constant.Tag> tags, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());


        Page<Note> notesPage = Optional.ofNullable(tags)
                .filter(__ -> !tags.isEmpty())
                .map(__ -> noteRepository.findByTagIn(tags, pageable))
                .orElseGet(() -> noteRepository.findAll(pageable));
        return noteMapper.toSummaryDTOList(notesPage.getContent());
    }

    public NoteResponse createNote(NoteRequest noteRequest) {
        return noteMapper.toResponseDTO(noteRepository.save(noteMapper.toEntity(noteRequest)));
    }

    public NoteResponse getNote(UUID id) {
        return noteRepository.findById(id)
                .map(noteMapper::toResponseDTO)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Note with ID %s not found", id)));
    }

    public NoteResponse updateNote(UUID id, NoteRequest noteRequest) {
        Note existingNote = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Note with ID %s not found", id)));
        existingNote.setTitle(noteRequest.getTitle());
        existingNote.setText(noteRequest.getText());
        existingNote.setTag(noteRequest.getTag());

        Note updatedNote = noteRepository.save(existingNote);
        return noteMapper.toResponseDTO(updatedNote);
    }

    public void deleteNote(UUID id) {
        if (!noteRepository.existsById(id)) {
            throw new ResourceNotFoundException(String.format("Note with ID %s not found", id));
        }
        noteRepository.deleteById(id);
    }

    public Map<String, Integer> getNoteTextStats(UUID id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Note with ID %s not found", id)));
        return calculateWordFrequency(note.getText());
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
