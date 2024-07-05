package com.teletronics.notesapi.mapper;

import com.teletronics.notesapi.model.NoteRequest;
import com.teletronics.notesapi.model.NoteResponse;
import com.teletronics.notesapi.model.NoteSummary;
import com.teletronics.notesapi.model.store.Note;
import org.mapstruct.*;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface NoteMapper {

    @Mapping(target = "id", ignore = true)
    Note toEntity(NoteRequest noteRequest);
    @IterableMapping(elementTargetType = NoteResponse.class)
    List<NoteResponse> toResponseDTOList(List<Note> note);
    NoteResponse toResponseDTO(Note note);
    @IterableMapping(elementTargetType = NoteSummary.class)
    List<NoteSummary> toSummaryDTOList(List<Note> note);
    NoteSummary toSummaryDTO(Note note);


    @AfterMapping
    default void setUUIDIfNotPresent(@MappingTarget Note note) {
        note.setId(UUID.randomUUID());
    }
}