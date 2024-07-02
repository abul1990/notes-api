package com.tele.notes_api.mapper;

import com.tele.notes_api.model.NoteRequest;
import com.tele.notes_api.model.NoteResponse;
import com.tele.notes_api.model.NoteSummary;
import com.tele.notes_api.model.store.Note;
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
        if (note.getId() == null) {
            note.setId(UUID.randomUUID());
        }
    }
}
