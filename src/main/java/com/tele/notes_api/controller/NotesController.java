package com.tele.notes_api.controller;


import com.tele.notes_api.model.Constant;
import com.tele.notes_api.model.NoteRequest;
import com.tele.notes_api.model.NoteResponse;
import com.tele.notes_api.model.NoteSummary;
import com.tele.notes_api.service.NoteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping(value = "/notes")
public class NotesController {

    private final NoteService noteService;

    @Autowired
    public NotesController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    public ResponseEntity<List<NoteSummary>> fetchNoteSummaries(@Valid @RequestParam(required = false) Set<Constant.Tag> tags,
                                                                @RequestParam(defaultValue = "0") @Min(0) int page,
                                                                @RequestParam(defaultValue = "10") @Min(1) int size) {
        return ResponseEntity.ok(noteService.fetchNoteSummaries(tags, page, size));
    }

    @PostMapping
    public ResponseEntity<NoteResponse> createNote(@Valid @RequestBody NoteRequest noteRequest) {
        return new ResponseEntity<>(noteService.createNote(noteRequest), HttpStatus.CREATED);
    }

    @GetMapping("/details")
    public ResponseEntity<List<NoteResponse>> fetchNotes(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                         @RequestParam(defaultValue = "10") @Min(1) int size) {
        return ResponseEntity.ok(noteService.fetchNotes(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteResponse> getNote(@PathVariable UUID id) {
        return ResponseEntity.ok(noteService.getNote(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoteResponse> updateNote(@PathVariable UUID id, @RequestBody NoteRequest noteRequest) {
        return ResponseEntity.ok(noteService.updateNote(id, noteRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable UUID id) {
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<Map<String, Integer>> getNoteStats(@PathVariable UUID id) {
        return ResponseEntity.ok(noteService.getNoteTextStats(id));
    }
}
